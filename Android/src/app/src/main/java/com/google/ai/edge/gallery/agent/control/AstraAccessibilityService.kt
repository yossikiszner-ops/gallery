package com.google.ai.edge.gallery.agent.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.atomic.AtomicReference

/** User-enabled Accessibility backend for universal UI control. */
class AstraAccessibilityService : AccessibilityService(), PhoneController {
  override val backend = ControlBackend.ACCESSIBILITY

  override fun onServiceConnected() {
    instance.set(this)
  }

  override fun onDestroy() {
    instance.compareAndSet(this, null)
    super.onDestroy()
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    event?.let { TrainingRecorder.record(it) }
  }

  override fun onInterrupt() = Unit

  override fun isAvailable(): Boolean = instance.get() === this

  override suspend fun observe(): PhoneObservation {
    val root = rootInActiveWindow
    val texts = mutableListOf<String>()
    collectText(root, texts, 0)
    return PhoneObservation(
      packageName = root?.packageName?.toString(),
      visibleText = texts.distinct().take(250),
      structuredUiAvailable = root != null,
    )
  }

  override suspend fun execute(action: PhoneAction): ActionResult {
    val ok = when (action) {
      is PhoneAction.OpenApp -> openApp(action.packageName)
      is PhoneAction.TapText -> tapText(action.text)
      is PhoneAction.TapPoint -> gesture(action.x, action.y, action.x, action.y, 80)
      is PhoneAction.TypeText -> typeText(action.text)
      is PhoneAction.Swipe -> gesture(action.startX, action.startY, action.endX, action.endY, 350)
      PhoneAction.Back -> performGlobalAction(GLOBAL_ACTION_BACK)
      PhoneAction.Home -> performGlobalAction(GLOBAL_ACTION_HOME)
      PhoneAction.Recents -> performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    return ActionResult(ok, backend, if (ok) "executed" else "action failed", observe())
  }

  private fun openApp(packageName: String): Boolean {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    return true
  }

  private fun tapText(text: String): Boolean {
    val root = rootInActiveWindow ?: return false
    val node = root.findAccessibilityNodeInfosByText(text).firstOrNull() ?: return false
    var current: AccessibilityNodeInfo? = node
    while (current != null) {
      if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
      current = current.parent
    }
    return false
  }

  private fun typeText(text: String): Boolean {
    val root = rootInActiveWindow ?: return false
    val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: findEditable(root)
      ?: return false
    val args = Bundle().apply {
      putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
    }
    return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
  }

  private fun findEditable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
    if (node == null) return null
    if (node.isEditable) return node
    for (i in 0 until node.childCount) findEditable(node.getChild(i))?.let { return it }
    return null
  }

  private fun collectText(node: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
    if (node == null || depth > 40) return
    node.text?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
    node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
    for (i in 0 until node.childCount) collectText(node.getChild(i), out, depth + 1)
  }

  private fun gesture(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long): Boolean {
    val path = Path().apply { moveTo(x1.toFloat(), y1.toFloat()); lineTo(x2.toFloat(), y2.toFloat()) }
    return dispatchGesture(
      GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, duration)).build(),
      null,
      null,
    )
  }

  companion object {
    private val instance = AtomicReference<AstraAccessibilityService?>(null)
    fun current(): AstraAccessibilityService? = instance.get()
  }
}
