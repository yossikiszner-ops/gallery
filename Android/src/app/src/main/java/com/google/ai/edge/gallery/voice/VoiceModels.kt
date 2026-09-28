package com.google.ai.edge.gallery.voice

enum class VoiceEngineId {
  SYSTEM_TTS,
  PIPER,
  KOKORO,
  CHATTERBOX,
  FISH_SPEECH,
}

data class VoiceEngineDescriptor(
  val id: VoiceEngineId,
  val displayName: String,
  val localFirst: Boolean,
  val downloadable: Boolean,
  val supportsHebrew: Boolean,
  val supportsEnglish: Boolean,
  val supportsVoiceConditioning: Boolean = false,
)

data class VoiceDescriptor(
  val engine: VoiceEngineId,
  val id: String,
  val displayName: String,
  val languageTags: Set<String>,
  val genderLabel: String? = null,
  val local: Boolean = true,
  val installed: Boolean = false,
)

object BuiltInVoiceEngines {
  val all = listOf(
    VoiceEngineDescriptor(VoiceEngineId.SYSTEM_TTS, "Android System TTS", true, false, true, true),
    VoiceEngineDescriptor(VoiceEngineId.PIPER, "Piper", true, true, true, true),
    VoiceEngineDescriptor(VoiceEngineId.KOKORO, "Kokoro", true, true, false, true),
    VoiceEngineDescriptor(VoiceEngineId.CHATTERBOX, "Chatterbox Multilingual", true, true, true, true, true),
    VoiceEngineDescriptor(VoiceEngineId.FISH_SPEECH, "Fish Speech", true, true, true, true, true),
  )
}

interface VoiceEngine {
  val descriptor: VoiceEngineDescriptor
  suspend fun listVoices(): List<VoiceDescriptor>
  suspend fun synthesize(text: String, voice: VoiceDescriptor, languageTag: String): ByteArray
}
