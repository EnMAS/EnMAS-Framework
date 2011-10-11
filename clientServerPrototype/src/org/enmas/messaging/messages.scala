package org.enmas.messaging

import org.enmas.pomdp._,
       org.enmas.server._,
       org.enmas.client._,
       akka.actor._


case class MessageBundle( content: List[Message] )


sealed trait Message


sealed trait ClientMessage extends Message { val recipient: ActorRef }


case class RegisterHost(
  hostRef: ActorRef,
  clientPublicKey: String
) extends Message


case class ConfirmHostRegistration(
  serverPublicKey: String,
  encryptedSharedKey: String
) extends Message


case object DenyHostRegistration extends Message


case class RegisterAgent(
  agentRef: ActorRef,
  agentType: AgentType
) extends Message


case class ConfirmAgentRegistration(
  recipient: ActorRef,
  agentType: AgentType,
  actions: List[Action]
) extends ClientMessage


case class DenyAgentRegistration( recipient: ActorRef ) extends ClientMessage


case class TakeAction( action: Action ) extends Message


case class UpdateAgent(
  recipient: ActorRef,
  observation: Observation,
  reward: Float
) extends ClientMessage
