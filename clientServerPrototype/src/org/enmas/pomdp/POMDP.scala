package org.enmas.pomdp

/** Represents a decentralized, partially observable Markov Decision Problem.
  */
case class POMDP(
  val name: String,
  val description: String,
  val agentConstraints: List[AgentConstraint],
  val initialState: State,
  val actionsFunction: AgentType => Set[Action],
  val transitionFunction: (State, JointAction) => State,
  val rewardFunction: (State, JointAction, State) => AgentType => Float,
  val observationFunction: (State, JointAction, State) => (Int, AgentType) => Observation
) {

  /** Checks that all supplied types are allowed and cardinalities do not
    * exceed limits.
    */
  def accomodatesAgents(agents: List[AgentType]): Boolean = {

    val allAllowed = agents.foldLeft(true){ (a,b) => {
      a && { agentConstraints map(_.agentType) contains(b) }}}

    val allUnderLimit = agentConstraints.foldLeft(true){ (a,b) => {
      a && { agents.filter(_ == b.agentType).length <= b.max }}}

    allAllowed && allUnderLimit
  }

  /** Checks that this model accomodates the agent set and that 
    * the agent set satisfies the minimum cardinalities.
    *
    * This is a more stringent test than the accomodatesAgents method.
    */
  def isSatisfiedByAgents(agents: List[AgentType]) : Boolean = {
    accomodatesAgents(agents) && { agentConstraints.foldLeft(true){ (a,b) => {
      a && agents.filter(_ == b.agentType).length >= b.min }}}
  }
}

case class AgentConstraint(
  agentType: AgentType,
  min: Int,
  max: Int
)