package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._

case class AgentRef(
  number: Int,
  ref: ActorRef,
  agentType: AgentType
)