package com.google.ai.edge.gallery.agent.security

import java.util.concurrent.CopyOnWriteArrayList

data class AgentActivity(
  val timestampMs: Long = System.currentTimeMillis(),
  val category: String,
  val summary: String,
  val success: Boolean,
)

/**
 * Small in-memory privacy-aware audit trail. Persistent storage can be opt-in later. Never place
 * API keys, message bodies, typed secrets, screenshots, or raw model prompts in summaries.
 */
class AgentActivityLog(private val maxEntries: Int = 250) {
  private val entries = CopyOnWriteArrayList<AgentActivity>()

  fun record(category: String, summary: String, success: Boolean) {
    entries += AgentActivity(category = category, summary = sanitize(summary), success = success)
    while (entries.size > maxEntries) entries.removeAt(0)
  }

  fun snapshot(): List<AgentActivity> = entries.toList().asReversed()
  fun clear() = entries.clear()

  private fun sanitize(value: String): String = value
    .replace(Regex("(?i)(api[_ -]?key|authorization|bearer)\\s*[:=]?\\s*\\S+"), "$1 [redacted]")
    .take(300)
}
