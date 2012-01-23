package org.enmas.messaging

import org.enmas.pomdp._,
       org.enmas.util.ServerSpec,
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
case class MessageBundle(content: List[AgentMessage])

sealed trait Message

sealed trait AgentMessage extends Message { val agentNumber: Int }

case class CreateServerFor(pomdp: POMDP)

/** Sent from a ClientManger to a Server
  */
case class RegisterHost(ref: ActorRef) extends Message

/** Sent from a Server to a ClientManager
  */
case class ConfirmHostRegistration(id: Int) extends Message

/** Sent from a ClientManager to a ServerManager
  */
case object Discovery extends Message

/** Sent from a ServerManager to ClientManager in
  * response to Discovery
  */
case class DiscoveryReply(
  servers: List[ServerSpec]
) extends Message

/** Sent from a Server to a ClientManager
  */
case object DenyHostRegistration extends Message

/** Sent from a ClientManger to a Server
  */
case class RegisterAgent(
  sessionID: Int,
  agentType: AgentType
) extends Message

/** Sent from a Server to a ClientManager session
  */
case class ConfirmAgentRegistration(
  agentNumber: Int,
  agentType: AgentType,
  actions: Set[Action]
) extends AgentMessage

/** Sent from a Server to a ClientManager session
  */
case object DenyAgentRegistration extends Message

/** Sent from an Agent to a ClientManager session, then
  * forwarded from that ClientManager session to a Server
  */
case class TakeAction(agentNumber: Int, action: Action ) extends Message

/** Sent from a Server to a ClientManager session, then
  * forwarded from that ClientManager session to an Agent
  */
case class UpdateAgent(
  agentNumber: Int,
  observation: Observation,
  reward: Float
) extends AgentMessage

/** Sent from a ClientManager session to a Server
*/
case class AgentDied(id: Int)

sealed trait Error extends Message { val cause: Throwable }
case class ClientError(cause: Throwable) extends Error
case class ServerError(cause: Throwable) extends Error