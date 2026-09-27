/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.google.ai.edge.gallery.agent.providers

/** Static metadata used by the provider setup UI and discovery engine. */
data class ProviderDefinition(
  val id: ProviderId,
  val displayName: String,
  val apiStyle: ApiStyle,
  val modelsEndpoint: String?,
  val keyPrefixHints: List<String> = emptyList(),
  val notes: String = "",
)

enum class ApiStyle { GOOGLE_GENERATIVE_LANGUAGE, OPENAI_COMPATIBLE, ANTHROPIC, CUSTOM }

/**
 * Provider metadata only. API keys are deliberately never stored here or in source control.
 * OPENAI_COMPATIBLE providers share one discovery/client implementation where possible.
 */
object ProviderCatalog {
  val all = listOf(
    ProviderDefinition(ProviderId.GOOGLE, "Google Gemini", ApiStyle.GOOGLE_GENERATIVE_LANGUAGE, "https://generativelanguage.googleapis.com/v1beta/models", listOf("AIza")),
    ProviderDefinition(ProviderId.OPENROUTER, "OpenRouter", ApiStyle.OPENAI_COMPATIBLE, "https://openrouter.ai/api/v1/models", listOf("sk-or-")),
    ProviderDefinition(ProviderId.NVIDIA, "NVIDIA NIM", ApiStyle.OPENAI_COMPATIBLE, "https://integrate.api.nvidia.com/v1/models", listOf("nvapi-")),
    ProviderDefinition(ProviderId.OPENAI, "OpenAI", ApiStyle.OPENAI_COMPATIBLE, "https://api.openai.com/v1/models", listOf("sk-")),
    ProviderDefinition(ProviderId.ANTHROPIC, "Anthropic", ApiStyle.ANTHROPIC, "https://api.anthropic.com/v1/models", listOf("sk-ant-")),
    ProviderDefinition(ProviderId.GROQ, "Groq", ApiStyle.OPENAI_COMPATIBLE, "https://api.groq.com/openai/v1/models", listOf("gsk_")),
    ProviderDefinition(ProviderId.CEREBRAS, "Cerebras", ApiStyle.OPENAI_COMPATIBLE, "https://api.cerebras.ai/v1/models"),
    ProviderDefinition(ProviderId.TOGETHER, "Together AI", ApiStyle.OPENAI_COMPATIBLE, "https://api.together.xyz/v1/models"),
    ProviderDefinition(ProviderId.MISTRAL, "Mistral AI", ApiStyle.OPENAI_COMPATIBLE, "https://api.mistral.ai/v1/models"),
    ProviderDefinition(ProviderId.DEEPSEEK, "DeepSeek", ApiStyle.OPENAI_COMPATIBLE, "https://api.deepseek.com/models"),
    ProviderDefinition(ProviderId.PERPLEXITY, "Perplexity", ApiStyle.OPENAI_COMPATIBLE, null, notes = "Model discovery may require catalog fallback."),
    ProviderDefinition(ProviderId.XAI, "xAI", ApiStyle.OPENAI_COMPATIBLE, "https://api.x.ai/v1/models"),
    ProviderDefinition(ProviderId.CUSTOM, "Custom / OpenAI-compatible", ApiStyle.CUSTOM, null, notes = "User supplies a base URL."),
  )

  fun get(id: ProviderId): ProviderDefinition = all.first { it.id == id }
}
