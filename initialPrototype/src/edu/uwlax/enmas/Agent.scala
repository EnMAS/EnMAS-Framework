package edu.uwlax.enmas

/** Defines an Agent within a POMDP.
  */
trait Agent {
  val name: Symbol
  val observationFunction: State => State
  val actionsFunction: State => Set[Action]
  val rewardFunction: State => Float

  /** Subclasses should override this to give the current
    * action chosen by the backing AI agent. */
  def action: Action = POMDP.NO_ACTION
}

/** Case class representation of an Agent. */
case class AgentCase (
  name: Symbol,
  observation: State,
  actions: Set[Action],
  reward: Float,
  action: Action
)
