package org.enmas.server

import akka.actor._

/** Represents a reference to a ClientManager from the point
 * of view of a Server.
 */
case class ClientManagerSpec(
  val id: Int,
  val ref: ActorRef
)
