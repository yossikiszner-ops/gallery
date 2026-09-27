package com.google.ai.edge.gallery.agent.providers

/** Credential material exists only for the duration of a discovery/request operation. */
data class ProviderCredential(
  val provider: AiProvider,
  val apiKey: String,
  val customBaseUrl: String? = null,
)

data class DiscoveryResult(
  val provider: AiProvider,
  val models: List<DiscoveredModel>,
  val warnings: List<String> = emptyList(),
)

interface ProviderDiscovery {
  suspend fun discover(credential: ProviderCredential): DiscoveryResult
  suspend fun probe(credential: ProviderCredential, model: DiscoveredModel): DiscoveredModel
}

/** Human-readable capability descriptions for the model picker. */
object ModelExplainer {
  fun tags(model: DiscoveredModel): List<String> = buildList {
    if (ModelCapability.TOOL_CALLING in model.capabilities) add("Tools")
    if (ModelCapability.VISION in model.capabilities) add("Vision")
    if (ModelCapability.REASONING in model.capabilities) add("Reasoning")
    if (ModelCapability.AUDIO_INPUT in model.capabilities) add("Audio input")
    if (ModelCapability.AUDIO_OUTPUT in model.capabilities) add("Audio output")
    if (ModelCapability.STREAMING in model.capabilities) add("Streaming")
  }

  fun bestFor(model: DiscoveredModel): List<String> = buildList {
    if (ModelCapability.TOOL_CALLING in model.capabilities) add("Agent and phone-control tasks")
    if (ModelCapability.VISION in model.capabilities) add("screen understanding")
    if (ModelCapability.REASONING in model.capabilities) add("complex planning")
    if ((model.contextWindow ?: 0) >= 100_000) add("long conversations and documents")
  }.distinct()
}
