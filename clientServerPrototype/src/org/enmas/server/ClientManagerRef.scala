package org.enmas.server

import akka.actor._,
       java.security._

case class ClientManagerRef (
  val channel: UntypedChannel,
  val hostname: String,
  val publicKey: PublicKey,
  var isIterationSubscriber: Boolean = false
)
