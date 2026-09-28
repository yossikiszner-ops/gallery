package com.google.ai.edge.gallery.agent.providers

data class ModelRequirements(
  val vision: Boolean = false,
  val tools: Boolean = false,
  val reasoning: Boolean = false,
  val audio: Boolean = false,
  val preferLowLatency: Boolean = true,
)

/**
 * Provider-neutral model selection. This intentionally uses capabilities rather than hard-coded
 * model names so newly discovered models can participate without an app update.
 */
object ModelRouter {
  fun choose(models: List<DiscoveredModel>, requirements: ModelRequirements): DiscoveredModel? {
    return models
      .asSequence()
      .filter { it.availability == ModelAvailability.AVAILABLE }
      .filter { !requirements.vision || ModelCapability.VISION in it.capabilities }
      .filter { !requirements.tools || ModelCapability.TOOL_CALLING in it.capabilities }
      .filter { !requirements.reasoning || ModelCapability.REASONING in it.capabilities }
      .filter {
        !requirements.audio ||
          ModelCapability.AUDIO_INPUT in it.capabilities ||
          ModelCapability.AUDIO_OUTPUT in it.capabilities
      }
      .sortedWith(
        compareByDescending<DiscoveredModel> { ModelCapability.TOOL_CALLING in it.capabilities }
          .thenByDescending { ModelCapability.REASONING in it.capabilities }
          .thenBy { if (requirements.preferLowLatency) it.latencyMs ?: Long.MAX_VALUE else 0L }
      )
      .firstOrNull()
  }
}
