package com.google.ai.edge.gallery.agent.training

import com.google.ai.edge.gallery.agent.control.PhoneAction

/** A locally learned workflow created from Training Mode or a successful Agent run. */
data class LearnedSkill(
  val id: String,
  val name: String,
  val triggerPhrases: List<String>,
  val actions: List<PhoneAction>,
  val enabled: Boolean = true,
  val source: SkillSource = SkillSource.TRAINING,
)

enum class SkillSource { TRAINING, AGENT_SUCCESS, USER_EDITED }

/** Keeps compilation separate so recorded accessibility events can be normalized before storage. */
object LearnedSkillCompiler {
  fun compile(name: String, triggerPhrases: List<String>, actions: List<PhoneAction>): LearnedSkill {
    require(name.isNotBlank())
    require(actions.isNotEmpty())
    return LearnedSkill(
      id = "learned-${System.currentTimeMillis()}",
      name = name.trim(),
      triggerPhrases = triggerPhrases.map(String::trim).filter(String::isNotBlank).distinct(),
      actions = actions,
    )
  }
}
