package com.google.ai.edge.gallery.agent.control

/** Result of one action after execution and verification. */
data class VerifiedActionResult(
  val success: Boolean,
  val attempts: List<ActionResult>,
  val finalObservation: PhoneObservation?,
)

fun interface ActionVerifier {
  fun verify(before: PhoneObservation?, after: PhoneObservation?, action: PhoneAction): Boolean
}

/**
 * Execute -> Observe -> Verify -> Recover. If a backend fails or verification says the UI did not
 * reach the expected state, the next available backend is tried. The planner remains independent.
 */
class PhoneAgentLoop(
  private val router: PhoneControlRouter,
  private val verifier: ActionVerifier,
) {
  suspend fun executeVerified(action: PhoneAction): VerifiedActionResult {
    val attempts = mutableListOf<ActionResult>()
    for (controller in router.availableControllers()) {
      val before = runCatching { controller.observe() }.getOrNull()
      val result = runCatching { controller.execute(action) }
        .getOrElse { ActionResult(false, controller.backend, it.message ?: "execution error") }
      attempts += result
      if (!result.success) continue
      val after = result.observationAfter ?: runCatching { controller.observe() }.getOrNull()
      if (verifier.verify(before, after, action)) {
        return VerifiedActionResult(true, attempts, after)
      }
    }
    return VerifiedActionResult(false, attempts, attempts.lastOrNull()?.observationAfter)
  }
}
