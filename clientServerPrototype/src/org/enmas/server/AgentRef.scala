package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._

case class AgentRef(
  clientManagerRef: ClientManagerRef,
  channel: UntypedChannel,
  agentNumber: Int,
  agentType: AgentType
)