package com.google.ai.edge.gallery.agent.control

/**
 * Optional elevated backend. This adapter deliberately does not assume Shizuku is installed or
 * authorized. The concrete bridge can be loaded only when the user explicitly enables it.
 */
interface ShizukuBridge {
  fun isInstalled(): Boolean
  fun hasPermission(): Boolean
  suspend fun executeShell(command: String): ShizukuResult
}

data class ShizukuResult(val success: Boolean, val stdout: String = "", val stderr: String = "")

class ShizukuController(private val bridge: ShizukuBridge) : PhoneController {
  override val backend = ControlBackend.SHIZUKU

  override fun isAvailable(): Boolean = bridge.isInstalled() && bridge.hasPermission()

  override suspend fun observe(): PhoneObservation = PhoneObservation()

  override suspend fun execute(action: PhoneAction): ActionResult {
    if (!isAvailable()) return ActionResult(false, backend, "Shizuku is unavailable or not authorized")
    val command = when (action) {
      is PhoneAction.OpenApp -> "monkey -p ${safePackage(action.packageName)} 1"
      is PhoneAction.TapPoint -> "input tap ${action.x} ${action.y}"
      is PhoneAction.Swipe -> "input swipe ${action.startX} ${action.startY} ${action.endX} ${action.endY} 350"
      PhoneAction.Back -> "input keyevent KEYCODE_BACK"
      PhoneAction.Home -> "input keyevent KEYCODE_HOME"
      PhoneAction.Recents -> "input keyevent KEYCODE_APP_SWITCH"
      // Text and semantic taps are safer through Accessibility because shell escaping is fragile.
      is PhoneAction.TypeText, is PhoneAction.TapText -> return ActionResult(false, backend, "Use Accessibility for this action")
    }
    val result = bridge.executeShell(command)
    return ActionResult(result.success, backend, result.stderr.ifBlank { result.stdout })
  }

  private fun safePackage(value: String): String {
    require(value.matches(Regex("[A-Za-z0-9._]+"))) { "Invalid package name" }
    return value
  }
}
