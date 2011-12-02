package org.enmas.server

import akka.actor._,
       java.security._

/** Represents a reference to a ClientManager from the point
 * of view of a Server.
 */
case class ClientManagerRef (
  val id: Int,
  val channel: ActorRef,
  val publicKey: PublicKey,
  val symmetricKey: Key
)
