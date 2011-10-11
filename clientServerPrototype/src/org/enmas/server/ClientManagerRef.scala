package org.enmas.server

import akka.actor._

case class ClientManagerRef (
  val ref: ActorRef,
  val host: Host,
  var isIterationSubscriber: Boolean
)
