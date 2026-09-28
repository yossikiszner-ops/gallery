package com.google.ai.edge.gallery.agent.providers

/** Cloud AI providers supported by the universal provider layer. */
enum class AiProvider(
  val displayName: String,
  val apiBaseUrl: String,
  val supportsModelDiscovery: Boolean = true,
) {
  GOOGLE("Google Gemini", "https://generativelanguage.googleapis.com"),
  OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
  NVIDIA("NVIDIA NIM", "https://integrate.api.nvidia.com/v1"),
  OPENAI("OpenAI", "https://api.openai.com/v1"),
  ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
  GROQ("Groq", "https://api.groq.com/openai/v1"),
  CEREBRAS("Cerebras", "https://api.cerebras.ai/v1"),
  TOGETHER("Together AI", "https://api.together.xyz/v1"),
  MISTRAL("Mistral AI", "https://api.mistral.ai/v1"),
  DEEPSEEK("DeepSeek", "https://api.deepseek.com"),
  XAI("xAI", "https://api.x.ai/v1"),
  CUSTOM_OPENAI_COMPATIBLE("Custom OpenAI-compatible", "", supportsModelDiscovery = false),
}

enum class ModelCapability {
  TEXT,
  VISION,
  AUDIO_INPUT,
  AUDIO_OUTPUT,
  TOOL_CALLING,
  STRUCTURED_OUTPUT,
  REASONING,
  STREAMING,
}

data class DiscoveredModel(
  val provider: AiProvider,
  val id: String,
  val displayName: String = id,
  val capabilities: Set<ModelCapability> = emptySet(),
  val contextWindow: Long? = null,
  val description: String = "",
  val availability: ModelAvailability = ModelAvailability.UNKNOWN,
  val latencyMs: Long? = null,
)

enum class ModelAvailability { UNKNOWN, AVAILABLE, UNAUTHORIZED, UNAVAILABLE, ERROR }

data class ProviderConnection(
  val provider: AiProvider,
  val customBaseUrl: String? = null,
  val enabled: Boolean = true,
)

/** API keys are deliberately NOT represented here. They belong in encrypted credential storage. */
interface ProviderModelDiscovery {
  val provider: AiProvider
  suspend fun discoverModels(apiKey: String, connection: ProviderConnection): List<DiscoveredModel>
  suspend fun probeModel(apiKey: String, model: DiscoveredModel): DiscoveredModel
}
