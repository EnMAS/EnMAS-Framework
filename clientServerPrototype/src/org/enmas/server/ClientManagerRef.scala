package org.enmas.server

import akka.actor._

class ClientManagerRef(
  ref: ActorRef,
  var agents: List[AgentRef]
)
