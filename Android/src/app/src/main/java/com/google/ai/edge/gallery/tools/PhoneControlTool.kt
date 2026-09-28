package com.google.ai.edge.gallery.tools

import com.google.ai.edge.gallery.agent.control.AstraAccessibilityService
import com.google.ai.edge.gallery.agent.control.PhoneAction
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/** Semantic phone-control tool. Accessibility must be explicitly enabled by the user in Android. */
class PhoneControlTool : ToolDefinition {
  override val alwaysAllow: Boolean = false
  override var executionContext: ToolExecutionContext? = null

  @Tool(description = "Observe visible text and the foreground Android app using the user-enabled Accessibility service.")
  fun observePhone(): Map<String, String> = runBlocking(Dispatchers.Default) {
    val controller = AstraAccessibilityService.current()
      ?: return@runBlocking mapOf("status" to "unavailable", "error" to "Accessibility service is not enabled")
    val observation = controller.observe()
    mapOf(
      "status" to "succeeded",
      "package" to observation.packageName.orEmpty(),
      "visible_text" to observation.visibleText.joinToString("\n").take(12_000),
    )
  }

  @Tool(description = "Tap a visible Android UI element containing the supplied text.")
  fun tapText(@ToolParam(description = "Visible text to tap") text: String): Map<String, String> =
    execute(PhoneAction.TapText(text))

  @Tool(description = "Type text into the currently focused or first editable Android field.")
  fun typeText(@ToolParam(description = "Text to type") text: String): Map<String, String> =
    execute(PhoneAction.TypeText(text))

  @Tool(description = "Open an installed Android app by package name.")
  fun openApp(@ToolParam(description = "Android package name") packageName: String): Map<String, String> =
    execute(PhoneAction.OpenApp(packageName))

  @Tool(description = "Press Android Back.") fun pressBack(): Map<String, String> = execute(PhoneAction.Back)
  @Tool(description = "Go to Android Home.") fun goHome(): Map<String, String> = execute(PhoneAction.Home)
  @Tool(description = "Open Android Recents.") fun openRecents(): Map<String, String> = execute(PhoneAction.Recents)

  @Tool(description = "Swipe on the screen between two coordinate points.")
  fun swipe(
    @ToolParam(description = "start x") startX: Int,
    @ToolParam(description = "start y") startY: Int,
    @ToolParam(description = "end x") endX: Int,
    @ToolParam(description = "end y") endY: Int,
  ): Map<String, String> = execute(PhoneAction.Swipe(startX, startY, endX, endY))

  private fun execute(action: PhoneAction): Map<String, String> = runBlocking(Dispatchers.Default) {
    val controller = AstraAccessibilityService.current()
      ?: return@runBlocking mapOf("status" to "unavailable", "error" to "Accessibility service is not enabled")
    val result = controller.execute(action)
    mapOf(
      "status" to if (result.success) "succeeded" else "failed",
      "backend" to result.backend.name,
      "message" to result.message,
      "package" to result.observationAfter?.packageName.orEmpty(),
    )
  }
}
