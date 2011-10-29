package org.enmas.messaging

import org.enmas.pomdp._,
       org.enmas.server._,
       org.enmas.client._,
       java.security._, java.security.interfaces._,
       akka.actor._

case class MessageBundle( content: List[AgentMessage] )

sealed trait Message

sealed trait AgentMessage extends Message { val agentNumber: Int }

case class RegisterHost(service: String, hostname: String, port: Int, clientPublicKey: PublicKey) extends Message

case class ConfirmHostRegistration(
  id: Int,
  serverPublicKey: PublicKey,
  encryptedSharedKey: Array[Byte]
) extends Message

case object DenyHostRegistration extends Message

case class RegisterAgent(clientManagerID: Int, agentType: AgentType) extends Message

case class ConfirmAgentRegistration(
  agentNumber: Int,
  agentType: AgentType,
  actions: Set[Action]
) extends Message

case object DenyAgentRegistration extends Message

case class TakeAction(agentID: Int, action: Action ) extends Message

case class UpdateAgent(
  agentNumber: Int,
  observation: Observation,
  reward: Float
) extends AgentMessage
