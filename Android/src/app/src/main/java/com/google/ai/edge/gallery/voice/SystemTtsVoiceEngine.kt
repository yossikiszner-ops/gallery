package com.google.ai.edge.gallery.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Uses every TTS voice installed on the Android device, including Hebrew voices when available. */
@Singleton
class SystemTtsVoiceEngine @Inject constructor(@ApplicationContext private val context: Context) : VoiceEngine {
  override val descriptor = BuiltInVoiceEngines.all.first { it.id == VoiceEngineId.SYSTEM_TTS }

  private suspend fun engine(): TextToSpeech = suspendCancellableCoroutine { cont ->
    lateinit var tts: TextToSpeech
    tts = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) cont.resume(tts)
      else cont.cancel(IllegalStateException("Android TTS initialization failed: $status"))
    }
  }

  override suspend fun listVoices(): List<VoiceDescriptor> {
    val tts = engine()
    return try {
      tts.voices.orEmpty().sortedBy { it.name }.map { voice ->
        VoiceDescriptor(
          engine = VoiceEngineId.SYSTEM_TTS,
          id = voice.name,
          displayName = "${voice.name} · ${voice.locale.displayName}",
          languageTags = setOf(voice.locale.toLanguageTag()),
          local = !voice.isNetworkConnectionRequired,
          installed = true,
        )
      }
    } finally { tts.shutdown() }
  }

  override suspend fun synthesize(text: String, voice: VoiceDescriptor, languageTag: String): ByteArray {
    val tts = engine()
    val file = File.createTempFile("astra_tts_", ".wav", context.cacheDir)
    val utteranceId = UUID.randomUUID().toString()
    return try {
      val selected = tts.voices?.firstOrNull { it.name == voice.id }
      if (selected != null) tts.voice = selected else tts.language = Locale.forLanguageTag(languageTag)
      suspendCancellableCoroutine { cont ->
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(id: String?) = Unit
          override fun onDone(id: String?) { if (id == utteranceId && cont.isActive) cont.resume(file.readBytes()) }
          @Deprecated("Deprecated in Android") override fun onError(id: String?) { if (cont.isActive) cont.cancel(IllegalStateException("TTS synthesis failed")) }
          override fun onError(id: String?, errorCode: Int) { if (cont.isActive) cont.cancel(IllegalStateException("TTS synthesis failed: $errorCode")) }
        })
        val result = tts.synthesizeToFile(text, Bundle(), file, utteranceId)
        if (result == TextToSpeech.ERROR && cont.isActive) cont.cancel(IllegalStateException("TTS request rejected"))
      }
    } finally {
      tts.shutdown()
      file.delete()
    }
  }
}
