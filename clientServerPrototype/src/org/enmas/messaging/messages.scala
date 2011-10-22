package org.enmas.messaging

import org.enmas.pomdp._,
       org.enmas.server._,
       org.enmas.client._,
       java.security._, java.security.interfaces._,
       akka.actor._

case class MessageBundle( content: List[Message] )

sealed trait Message {}

sealed trait ClientMessage extends Message { val recipient: UntypedChannel }

case class RegisterHost(hostname: String, clientPublicKey: PublicKey) extends Message

case class ConfirmHostRegistration(
  serverPublicKey: PublicKey,
  encryptedSharedKey: String
) extends Message

case object DenyHostRegistration extends Message

case class RegisterAgent(agentType: AgentType) extends Message

case class ConfirmAgentRegistration(
  recipient: UntypedChannel,
  agentNumber: Int,
  agentType: AgentType,
  actions: Set[Action]
) extends ClientMessage

case class DenyAgentRegistration( recipient: UntypedChannel ) extends ClientMessage

case class TakeAction( action: Action ) extends Message

case class UpdateAgent(
  recipient: UntypedChannel,
  observation: Observation,
  reward: Float
) extends ClientMessage
