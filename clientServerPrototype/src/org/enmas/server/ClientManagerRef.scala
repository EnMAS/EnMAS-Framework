package org.enmas.server

import akka.actor._

class ClientManagerRef(
  val ref: ActorRef,
  var agents: List[AgentRef],
  var isIterationSubscriber: Boolean
)
