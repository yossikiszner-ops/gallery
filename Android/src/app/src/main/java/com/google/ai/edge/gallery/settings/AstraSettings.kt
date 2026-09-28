package com.google.ai.edge.gallery.settings

import com.google.ai.edge.gallery.agent.security.AgentPermissionSet
import com.google.ai.edge.gallery.agent.security.AutonomyMode
import com.google.ai.edge.gallery.voice.VoiceSettings
import com.google.ai.edge.gallery.voice.WakeWordProfile

enum class AppLanguage { SYSTEM, ENGLISH, HEBREW }
enum class ModelSelectionMode { MANUAL, AUTO }

data class AstraSettings(
  val language: AppLanguage = AppLanguage.SYSTEM,
  val modelSelectionMode: ModelSelectionMode = ModelSelectionMode.AUTO,
  val autonomyMode: AutonomyMode = AutonomyMode.ASK_FOR_SENSITIVE,
  val permissions: AgentPermissionSet = AgentPermissionSet(),
  val voice: VoiceSettings = VoiceSettings(),
  val wakeWord: WakeWordProfile = WakeWordProfile(phrase = "Astra"),
  val trainingEnabled: Boolean = false,
  val localFirst: Boolean = true,
  val allowCloudFallback: Boolean = false,
  val activityLogEnabled: Boolean = true,
)
