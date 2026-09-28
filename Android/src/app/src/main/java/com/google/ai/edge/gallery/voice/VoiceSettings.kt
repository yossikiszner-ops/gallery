package com.google.ai.edge.gallery.voice

enum class VoiceSelectionMode { SYSTEM_DEFAULT, FIXED, AUTO }
enum class VoiceQuality { FAST, BALANCED, HIGH }

data class VoiceSettings(
  val mode: VoiceSelectionMode = VoiceSelectionMode.AUTO,
  val defaultEngine: VoiceEngineId = VoiceEngineId.SYSTEM_TTS,
  val defaultVoiceId: String? = null,
  val hebrewEngine: VoiceEngineId? = null,
  val hebrewVoiceId: String? = null,
  val englishEngine: VoiceEngineId? = null,
  val englishVoiceId: String? = null,
  val quality: VoiceQuality = VoiceQuality.BALANCED,
  val speed: Float = 1f,
  val pitch: Float = 1f,
)

class VoiceEngineRegistry(engines: List<VoiceEngine>) {
  private val enginesById = engines.associateBy { it.descriptor.id }

  fun installed(): List<VoiceEngine> = enginesById.values.toList()
  fun get(id: VoiceEngineId): VoiceEngine? = enginesById[id]

  suspend fun allVoices(): List<VoiceDescriptor> = enginesById.values.flatMap { engine ->
    runCatching { engine.listVoices() }.getOrDefault(emptyList())
  }

  fun choose(settings: VoiceSettings, languageTag: String, textLength: Int): VoiceEngine? {
    if (settings.mode == VoiceSelectionMode.SYSTEM_DEFAULT) return get(VoiceEngineId.SYSTEM_TTS)
    if (settings.mode == VoiceSelectionMode.FIXED) return get(settings.defaultEngine)
    val isHebrew = languageTag.startsWith("he", ignoreCase = true)
    val explicit = if (isHebrew) settings.hebrewEngine else settings.englishEngine
    explicit?.let { get(it)?.let { engine -> return engine } }
    val preference = when {
      settings.quality == VoiceQuality.FAST || textLength < 80 -> listOf(VoiceEngineId.SYSTEM_TTS, VoiceEngineId.PIPER)
      settings.quality == VoiceQuality.HIGH -> listOf(VoiceEngineId.CHATTERBOX, VoiceEngineId.FISH_SPEECH, VoiceEngineId.KOKORO, VoiceEngineId.PIPER, VoiceEngineId.SYSTEM_TTS)
      else -> listOf(VoiceEngineId.PIPER, VoiceEngineId.SYSTEM_TTS, VoiceEngineId.CHATTERBOX, VoiceEngineId.FISH_SPEECH, VoiceEngineId.KOKORO)
    }
    return preference.mapNotNull(::get).firstOrNull { engine ->
      if (isHebrew) engine.descriptor.supportsHebrew else engine.descriptor.supportsEnglish
    }
  }
}
