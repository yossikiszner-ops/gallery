package com.google.ai.edge.gallery.agent.providers

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Live model discovery for Google, Anthropic and OpenAI-compatible providers. */
class HttpProviderDiscovery : ProviderDiscovery {
  override suspend fun discover(credential: ProviderCredential): DiscoveryResult = withContext(Dispatchers.IO) {
    val definition = ProviderCatalog.get(credential.provider)
    val endpoint = when {
      credential.provider == AiProvider.CUSTOM_OPENAI_COMPATIBLE ->
        credential.customBaseUrl?.trimEnd('/')?.plus("/models")
      else -> definition.modelsEndpoint
    } ?: return@withContext DiscoveryResult(credential.provider, emptyList(), listOf("This provider does not expose model discovery; use its documented model catalog."))

    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = 12_000
      readTimeout = 20_000
      setRequestProperty("Accept", "application/json")
      when (definition.apiStyle) {
        ApiStyle.GOOGLE_GENERATIVE_LANGUAGE -> setRequestProperty("x-goog-api-key", credential.apiKey)
        ApiStyle.ANTHROPIC -> {
          setRequestProperty("x-api-key", credential.apiKey)
          setRequestProperty("anthropic-version", "2023-06-01")
        }
        else -> setRequestProperty("Authorization", "Bearer ${credential.apiKey}")
      }
    }

    try {
      val code = connection.responseCode
      val stream = if (code in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
      if (code !in 200..299) {
        val warning = when (code) {
          401, 403 -> "Key was rejected or does not have permission for model discovery."
          429 -> "Provider rate limit reached; try again later."
          else -> "Provider returned HTTP $code."
        }
        return@withContext DiscoveryResult(credential.provider, emptyList(), listOf(warning))
      }
      DiscoveryResult(credential.provider, parseModels(credential.provider, definition.apiStyle, body))
    } finally {
      connection.disconnect()
    }
  }

  override suspend fun probe(credential: ProviderCredential, model: DiscoveredModel): DiscoveredModel {
    val started = System.nanoTime()
    val discovered = discover(credential).models.firstOrNull { it.id == model.id }
    val latency = (System.nanoTime() - started) / 1_000_000
    return if (discovered != null) discovered.copy(availability = ModelAvailability.AVAILABLE, latencyMs = latency)
    else model.copy(availability = ModelAvailability.UNAVAILABLE, latencyMs = latency)
  }

  private fun parseModels(provider: AiProvider, style: ApiStyle, body: String): List<DiscoveredModel> {
    val root = JSONObject(body)
    val array = if (style == ApiStyle.GOOGLE_GENERATIVE_LANGUAGE) root.optJSONArray("models") else root.optJSONArray("data")
      ?: return emptyList()
    return buildList {
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i) ?: continue
        val rawId = obj.optString("id").ifBlank { obj.optString("name") }
        if (rawId.isBlank()) continue
        val id = rawId.removePrefix("models/")
        val displayName = obj.optString("displayName").ifBlank { id }
        val description = obj.optString("description")
        val context = obj.optLong("inputTokenLimit", 0L).takeIf { it > 0 }
        add(
          DiscoveredModel(
            provider = provider,
            id = id,
            displayName = displayName,
            capabilities = inferCapabilities(id, obj),
            contextWindow = context,
            description = description,
            availability = ModelAvailability.AVAILABLE,
          )
        )
      }
    }
  }

  private fun inferCapabilities(id: String, metadata: JSONObject): Set<ModelCapability> {
    val name = id.lowercase()
    return buildSet {
      add(ModelCapability.TEXT)
      add(ModelCapability.STREAMING)
      if (listOf("vision", "vl", "gemini", "gpt-4", "gpt-5", "claude", "pixtral").any(name::contains)) add(ModelCapability.VISION)
      if (listOf("reason", "thinking", "o1", "o3", "o4", "r1").any(name::contains)) add(ModelCapability.REASONING)
      val methods = metadata.optJSONArray("supportedGenerationMethods")
      if (methods != null && (0 until methods.length()).any { methods.optString(it).contains("generate", true) }) add(ModelCapability.STRUCTURED_OUTPUT)
      // Tool support is confirmed later by provider-specific capability probes, not guessed here.
    }
  }
}
