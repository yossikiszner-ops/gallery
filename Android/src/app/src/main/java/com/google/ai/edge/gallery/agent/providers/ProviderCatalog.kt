package com.google.ai.edge.gallery.agent.providers

/** Static metadata used by the provider setup UI and discovery engine. */
data class ProviderDefinition(
  val provider: AiProvider,
  val apiStyle: ApiStyle,
  val modelsEndpoint: String?,
  val keyPrefixHints: List<String> = emptyList(),
  val notes: String = "",
)

enum class ApiStyle { GOOGLE_GENERATIVE_LANGUAGE, OPENAI_COMPATIBLE, ANTHROPIC, CUSTOM }

/** Provider metadata only. API keys are never stored here or in source control. */
object ProviderCatalog {
  val all = listOf(
    ProviderDefinition(AiProvider.GOOGLE, ApiStyle.GOOGLE_GENERATIVE_LANGUAGE, "https://generativelanguage.googleapis.com/v1beta/models", listOf("AIza")),
    ProviderDefinition(AiProvider.OPENROUTER, ApiStyle.OPENAI_COMPATIBLE, "https://openrouter.ai/api/v1/models", listOf("sk-or-")),
    ProviderDefinition(AiProvider.NVIDIA, ApiStyle.OPENAI_COMPATIBLE, "https://integrate.api.nvidia.com/v1/models", listOf("nvapi-")),
    ProviderDefinition(AiProvider.OPENAI, ApiStyle.OPENAI_COMPATIBLE, "https://api.openai.com/v1/models", listOf("sk-")),
    ProviderDefinition(AiProvider.ANTHROPIC, ApiStyle.ANTHROPIC, "https://api.anthropic.com/v1/models", listOf("sk-ant-")),
    ProviderDefinition(AiProvider.GROQ, ApiStyle.OPENAI_COMPATIBLE, "https://api.groq.com/openai/v1/models", listOf("gsk_")),
    ProviderDefinition(AiProvider.CEREBRAS, ApiStyle.OPENAI_COMPATIBLE, "https://api.cerebras.ai/v1/models"),
    ProviderDefinition(AiProvider.TOGETHER, ApiStyle.OPENAI_COMPATIBLE, "https://api.together.xyz/v1/models"),
    ProviderDefinition(AiProvider.MISTRAL, ApiStyle.OPENAI_COMPATIBLE, "https://api.mistral.ai/v1/models"),
    ProviderDefinition(AiProvider.DEEPSEEK, ApiStyle.OPENAI_COMPATIBLE, "https://api.deepseek.com/models"),
    ProviderDefinition(AiProvider.XAI, ApiStyle.OPENAI_COMPATIBLE, "https://api.x.ai/v1/models"),
    ProviderDefinition(AiProvider.CUSTOM_OPENAI_COMPATIBLE, ApiStyle.CUSTOM, null, notes = "User supplies a base URL."),
  )

  fun get(provider: AiProvider): ProviderDefinition = all.first { it.provider == provider }
}
