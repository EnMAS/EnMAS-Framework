package org.enmas.pomdp

/** Represents a decentralized, partially observable Markov Decision Problem. */
case class POMDP(
  val name: String,
  val description: String,
  val agents: List[AgentConstraint],
  val initialState: State,
  val actionsFunction: AgentType => List[Action],
  val transitionFunction: (State, JointAction) => State,
  val rewardFunction: (State, JointAction) => AgentType => Float,
  val observationFunction: (State, JointAction) => AgentType => Observation
)

case class AgentConstraint(
  agentType: AgentType,
  minAgents: Int,
  maxAgents: Int
)