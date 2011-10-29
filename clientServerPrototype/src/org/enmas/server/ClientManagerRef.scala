package org.enmas.server

import akka.actor._,
       java.security._

case class ClientManagerRef (
  val id: Int,
  val channel: ActorRef,
  val publicKey: PublicKey,
  val symmetricKey: Key
)
