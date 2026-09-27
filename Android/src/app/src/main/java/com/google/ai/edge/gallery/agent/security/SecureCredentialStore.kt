package com.google.ai.edge.gallery.agent.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.ai.edge.gallery.agent.providers.AiProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only provider secret storage. Keys are encrypted with a non-exportable Android Keystore
 * AES key. The plaintext API key is never written to DataStore, SharedPreferences, logs, or Git.
 */
@Singleton
class SecureCredentialStore @Inject constructor(@ApplicationContext context: Context) {
  private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
  private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

  fun put(provider: AiProvider, apiKey: String) {
    require(apiKey.isNotBlank()) { "API key must not be blank" }
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
    val ciphertext = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))
    prefs.edit()
      .putString("${provider.name}.ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
      .putString("${provider.name}.iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
      .apply()
  }

  fun get(provider: AiProvider): String? {
    val encoded = prefs.getString("${provider.name}.ciphertext", null) ?: return null
    val encodedIv = prefs.getString("${provider.name}.iv", null) ?: return null
    return runCatching {
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(
        Cipher.DECRYPT_MODE,
        getOrCreateKey(),
        GCMParameterSpec(128, Base64.decode(encodedIv, Base64.NO_WRAP)),
      )
      String(cipher.doFinal(Base64.decode(encoded, Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()
  }

  fun remove(provider: AiProvider) {
    prefs.edit().remove("${provider.name}.ciphertext").remove("${provider.name}.iv").apply()
  }

  fun has(provider: AiProvider): Boolean = prefs.contains("${provider.name}.ciphertext")

  private fun getOrCreateKey(): SecretKey {
    (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    generator.init(
      KeyGenParameterSpec.Builder(
          KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()
    )
    return generator.generateKey()
  }

  private companion object {
    const val PREFS = "astra_secure_provider_credentials"
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val KEY_ALIAS = "astra_provider_credentials_v1"
    const val TRANSFORMATION = "AES/GCM/NoPadding"
  }
}
