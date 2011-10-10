package org.enmas.server

import org.enmas.pomdp._

case class POMDPIteration(
  observations: Set[(AgentRef, State)],
  rewards: Set[(AgentRef, Float)],
  actions: Set[(AgentRef, Action)],
  state: State
)
