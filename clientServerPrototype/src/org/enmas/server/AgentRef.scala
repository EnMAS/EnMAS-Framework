package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._

case class AgentRef(
  clientManagerID: Int,
  agentNumber: Int,
  agentType: AgentType
)