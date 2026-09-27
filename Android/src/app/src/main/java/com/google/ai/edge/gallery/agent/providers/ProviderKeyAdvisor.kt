package com.google.ai.edge.gallery.agent.providers

/**
 * Gives local, non-network hints before discovery. The user still chooses the provider first;
 * prefixes are only used to catch obvious mistakes and never to transmit or log a key.
 */
object ProviderKeyAdvisor {
  data class Advice(val plausible: Boolean, val message: String)

  fun inspect(provider: AiProvider, apiKey: String): Advice {
    if (apiKey.isBlank()) return Advice(false, "API key is empty")
    val definition = ProviderCatalog.get(provider)
    if (definition.keyPrefixHints.isEmpty()) {
      return Advice(true, "Key format will be verified with the provider")
    }
    val prefixMatches = definition.keyPrefixHints.any(apiKey::startsWith)
    return if (prefixMatches) {
      Advice(true, "Key format looks plausible; verify by discovering models")
    } else {
      Advice(
        true,
        "Key prefix is unusual for ${provider.displayName}; you can still verify it with the provider",
      )
    }
  }
}
