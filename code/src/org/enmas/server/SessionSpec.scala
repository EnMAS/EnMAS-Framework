package org.enmas.server

import akka.actor._

/** Represents a reference to a Session from the point
 * of view of a Server.
 */
case class SessionSpec(
  val id: Int,
  val ref: ActorRef
)