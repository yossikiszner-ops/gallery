/*
 * Copyright 2026 Google LLC
 * Licensed under the Apache License, Version 2.0.
 */
package com.google.ai.edge.gallery.agent.providers

/** Credential material is passed only for the duration of a discovery/request operation. */
data class ProviderCredential(
  val providerId: ProviderId,
  val apiKey: String,
  val customBaseUrl: String? = null,
)

data class DiscoveryResult(
  val providerId: ProviderId,
  val models: List<RemoteModel>,
  val warnings: List<String> = emptyList(),
)

/** Network implementation is intentionally isolated from settings/UI and secure storage. */
interface ProviderDiscovery {
  suspend fun discover(credential: ProviderCredential): DiscoveryResult
  suspend fun probe(credential: ProviderCredential, model: RemoteModel): ModelProbeResult
}

data class ModelProbeResult(
  val modelId: String,
  val reachable: Boolean,
  val latencyMs: Long? = null,
  val capabilities: ModelCapabilities = ModelCapabilities(),
  val failureReason: String? = null,
)

/**
 * Produces human-readable tags without hard-coding a single "best model". The router can use
 * the same capability data independently.
 */
object ModelExplainer {
  fun tags(model: RemoteModel): List<String> = buildList {
    if (model.capabilities.toolCalling) add("Tools")
    if (model.capabilities.vision) add("Vision")
    if (model.capabilities.reasoning) add("Reasoning")
    if (model.capabilities.fast) add("Fast")
    if (model.capabilities.longContext) add("Long context")
  }

  fun bestFor(model: RemoteModel): List<String> = buildList {
    if (model.capabilities.toolCalling && model.capabilities.fast) add("everyday Agent tasks")
    if (model.capabilities.vision) add("screen understanding")
    if (model.capabilities.reasoning) add("complex planning")
    if (model.capabilities.longContext) add("long conversations and documents")
  }.distinct()
}
