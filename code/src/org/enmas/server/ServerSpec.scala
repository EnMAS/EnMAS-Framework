package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._

/** Represents a reference to a Server from the point of
  * view of a ServerManager or ClientManager
  */
case class ServerSpec(
  ref: ActorRef,
  model: POMDP
)
