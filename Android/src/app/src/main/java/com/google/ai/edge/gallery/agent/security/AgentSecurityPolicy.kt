/*
 * Copyright 2026 Google LLC
 * Licensed under the Apache License, Version 2.0.
 */
package com.google.ai.edge.gallery.agent.security

enum class AutonomyMode {
  /** Execute ordinary actions; require explicit confirmation for sensitive actions. */
  ASK_FOR_SENSITIVE,
  /** Execute permitted actions without confirmation. System safety boundaries still apply. */
  FULL_AUTONOMY,
}

enum class ActionRisk {
  ORDINARY,
  SEND_MESSAGE,
  DELETE_DATA,
  PURCHASE_OR_PAYMENT,
  ACCOUNT_OR_PERMISSION_CHANGE,
  INSTALL_OR_UNINSTALL,
  SHARE_PRIVATE_DATA,
  SYSTEM_SETTING,
}

data class AgentPermissionSet(
  val accessibility: Boolean = false,
  val shizuku: Boolean = false,
  val screenCapture: Boolean = false,
  val messaging: Boolean = false,
  val files: Boolean = false,
  val systemSettings: Boolean = false,
  val mcp: Boolean = false,
  val memory: Boolean = false,
)

data class SecurityDecision(val allowed: Boolean, val confirmationRequired: Boolean, val reason: String)

object AgentSecurityPolicy {
  fun evaluate(mode: AutonomyMode, risk: ActionRisk, capabilityGranted: Boolean): SecurityDecision {
    if (!capabilityGranted) {
      return SecurityDecision(false, false, "Required capability is disabled by the user")
    }
    val sensitive = risk != ActionRisk.ORDINARY
    return if (mode == AutonomyMode.ASK_FOR_SENSITIVE && sensitive) {
      SecurityDecision(true, true, "Sensitive action requires confirmation")
    } else {
      SecurityDecision(true, false, "Allowed by current autonomy settings")
    }
  }
}
