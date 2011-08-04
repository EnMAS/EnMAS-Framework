package edu.uwlax.enmas

/** Defines an Agent within a POMDP.
  */
trait Agent {
  val name: Symbol
  val observation: State => State             // observation
  val actions: State => Map[String, Action]   // actions
  val reward: State => Float                  // reward

  /** Subclasses should override this to give the current
    * action chosen by the backing AI agent. */
  def action: Action = POMDP.NO_ACTION

  /** Implicit conversion to low-memory representation. */
  implicit final def toCaseClass: AgentCase = {
    AgentCase(name, observation, actions, reward, action)
  }
}

/** Case class representation of an Agent. */
case class AgentCase (
  name: Symbol,
  observation: State => State,              // observation
  actions: State => Map[String, Action],    // actions
  reward: State => Float,                   // reward
  action: Action
)
