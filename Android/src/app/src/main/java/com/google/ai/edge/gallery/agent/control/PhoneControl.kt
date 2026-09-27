/*
 * Copyright 2026 Google LLC
 * Licensed under the Apache License, Version 2.0.
 */
package com.google.ai.edge.gallery.agent.control

/** Ordered control backends. Prefer structured Android APIs before UI automation. */
enum class ControlBackend { ANDROID_API, ACCESSIBILITY, SHIZUKU, VISION }

data class PhoneObservation(
  val packageName: String? = null,
  val visibleText: List<String> = emptyList(),
  val structuredUiAvailable: Boolean = false,
  val screenshotAvailable: Boolean = false,
)

sealed interface PhoneAction {
  data class OpenApp(val packageName: String) : PhoneAction
  data class TapText(val text: String) : PhoneAction
  data class TapPoint(val x: Int, val y: Int) : PhoneAction
  data class TypeText(val text: String) : PhoneAction
  data class Swipe(val startX: Int, val startY: Int, val endX: Int, val endY: Int) : PhoneAction
  data object Back : PhoneAction
  data object Home : PhoneAction
  data object Recents : PhoneAction
}

data class ActionResult(
  val success: Boolean,
  val backend: ControlBackend,
  val message: String = "",
  val observationAfter: PhoneObservation? = null,
)

interface PhoneController {
  val backend: ControlBackend
  fun isAvailable(): Boolean
  suspend fun observe(): PhoneObservation
  suspend fun execute(action: PhoneAction): ActionResult
}

/**
 * Chooses the least-privileged available backend first. A future executor can retry the next
 * backend after verification fails, giving the Agent its Observe -> Act -> Verify -> Recover loop.
 */
class PhoneControlRouter(private val controllers: List<PhoneController>) {
  private val preference = listOf(
    ControlBackend.ANDROID_API,
    ControlBackend.ACCESSIBILITY,
    ControlBackend.SHIZUKU,
    ControlBackend.VISION,
  )

  fun availableControllers(): List<PhoneController> = preference.mapNotNull { wanted ->
    controllers.firstOrNull { it.backend == wanted && it.isAvailable() }
  }
}
