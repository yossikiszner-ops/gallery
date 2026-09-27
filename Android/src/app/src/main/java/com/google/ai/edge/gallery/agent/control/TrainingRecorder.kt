package com.google.ai.edge.gallery.agent.control

import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory recorder for explicit Agent Training sessions. It records semantic accessibility events,
 * not video or raw screen frames. Persisting a generated skill is a separate, user-confirmed step.
 */
object TrainingRecorder {
  data class Step(
    val eventType: Int,
    val packageName: String?,
    val className: String?,
    val text: List<String>,
    val timestamp: Long,
  )

  private val steps = CopyOnWriteArrayList<Step>()
  @Volatile private var recording = false

  fun start() { steps.clear(); recording = true }
  fun stop(): List<Step> { recording = false; return steps.toList() }
  fun cancel() { recording = false; steps.clear() }
  fun isRecording(): Boolean = recording

  fun record(event: AccessibilityEvent) {
    if (!recording) return
    when (event.eventType) {
      AccessibilityEvent.TYPE_VIEW_CLICKED,
      AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
      AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
      AccessibilityEvent.TYPE_VIEW_SCROLLED,
      AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
        steps += Step(
          eventType = event.eventType,
          packageName = event.packageName?.toString(),
          className = event.className?.toString(),
          text = event.text.map(CharSequence::toString).filter(String::isNotBlank).take(10),
          timestamp = System.currentTimeMillis(),
        )
      }
    }
  }
}
