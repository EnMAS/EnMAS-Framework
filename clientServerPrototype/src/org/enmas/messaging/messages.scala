package org.enmas.messaging

import org.enmas.pomdp._,
       org.enmas.server._,
       org.enmas.client._,
       java.security._, java.security.interfaces._,
       akka.actor._

/** Wraps a series of Messages destined for agents 
  * cohabitating on a given host.  The ClientManager
  * allows for the creation of many agents per host,
  * so this takes advantage of that to reduce unnecessay
  * network overhead and send/receive events.
  */
case class MessageBundle( content: List[AgentMessage] )

sealed trait Message

sealed trait AgentMessage extends Message { val agentNumber: Int }

/** Sent from a ClientManger to a Server
  */
case class RegisterHost(
  service: String,
  hostname: String,
  port: Int,
  clientPublicKey: PublicKey
) extends Message

/** Sent from a Server to a ClientManager
  */
case class ConfirmHostRegistration(
  id: Int,
  serverPublicKey: PublicKey,
  encryptedSharedKey: Array[Byte]
) extends Message

case object Discovery extends Message

case class DiscoveryReply(
  host: String,
  service: String,
  port: Int,
  modelName: String,
  iterating: Boolean
) extends Message

/** Sent from a Server to a ClientManager
  */
case object DenyHostRegistration extends Message

/** Sent from a ClientManger to a Server
  */
case class RegisterAgent(clientManagerID: Int, agentType: AgentType) extends Message

/** Sent from a Server to a ClientManager
  */
case class ConfirmAgentRegistration(
  agentNumber: Int,
  agentType: AgentType,
  actions: Set[Action]
) extends AgentMessage

/** Sent from a Server to a ClientManager
  */
case object DenyAgentRegistration extends Message

/** Sent from an Agent to a ClientManager, then
  * forwarded from that ClientManager to a Server
  */
case class TakeAction(agentNumber: Int, action: Action ) extends Message

/** Sent from a Server to a ClientManager, then
  * forwarded from that ClientManager to an Agent
  */
case class UpdateAgent(
  agentNumber: Int,
  observation: Observation,
  reward: Float
) extends AgentMessage

sealed trait Error { val cause: Throwable }
case class ClientError(cause: Throwable) extends Error
case class ServerError(cause: Throwable) extends Error