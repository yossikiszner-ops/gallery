package com.google.ai.edge.gallery.voice

/** Wake word is optional and user-controlled. Audio templates should remain local by default. */
data class WakeWordProfile(
  val phrase: String,
  val enabled: Boolean = false,
  val enrollmentComplete: Boolean = false,
  val sampleCount: Int = 0,
)

data class WakeWordEnrollmentPrompt(val index: Int, val text: String)

object WakeWordEnrollment {
  fun prompts(phrase: String): List<WakeWordEnrollmentPrompt> = listOf(
    "Say ‘$phrase’ normally",
    "Say ‘$phrase’ a little more quietly",
    "Say ‘$phrase’ from a short distance",
    "Say ‘$phrase’ naturally again",
    "Say ‘$phrase’ one last time",
  ).mapIndexed { index, text -> WakeWordEnrollmentPrompt(index + 1, text) }
}

interface WakeWordEngine {
  val id: String
  suspend fun enroll(profile: WakeWordProfile, pcm16Samples: List<ByteArray>): WakeWordProfile
  suspend fun start(profile: WakeWordProfile, onDetected: suspend () -> Unit)
  suspend fun stop()
}
