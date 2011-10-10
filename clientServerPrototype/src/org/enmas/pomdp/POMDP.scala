package org.enmas.pomdp

/** Represents a decentralized, partially observable Markov Decision Problem. */
case class POMDP(

  val name: String,

  val description: String,
  
  // The 2nd and 3rd items of 3-tuple are interpreted to be min and max.
  // i.e.
  // initialState = List(('Agent1, 1, 1), ('Agent2, 1, 2))
  // to specify exactly one agent of type 'Agent1
  // and at least 1 but not more than 2 agents of type 'Agent2
  val agents: List[(AgentType, Int, Int)],

  val initialState: State,

  val actionsFunction: AgentType => List[Action],

  val transitionFunction: (State, JointAction) => State,

  // the Int here is a unique agent number.  Agent numbers are numbered from 1 to n
  val rewardFunction: (State, JointAction) => List[(Int, Float)],

  // the Int here is a unique agent number.  Agent numbers are numbered from 1 to n
  val observationFunction: (State, JointAction) => List[(Int, State)]

)
