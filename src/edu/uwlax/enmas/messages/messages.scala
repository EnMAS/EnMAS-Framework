package edu.uwlax.enmas.messages {

  import edu.uwlax.enmas._
  import scala.actors._

  /** Parent class for all protocol messages. */
  sealed trait Message

  /** Sent by a new ClientAgent during initialization.  The SimServer instance
    * listens for these and responds with RegisterConfirmation.
    *
    * This class only concerns API designers; ClientAgent registration with 
    * the server takes place transparently to end users.
    *
    * @see edu.uwlax.enmas.server.SimServer
    * @see edu.uwlax.enmas.client.ClientAgent
    */
  case class Register(host: String, port: Int, name: Symbol) extends Message

  /** Sent by the SimServer instance during the registration handshake as a
    * reply to the Register message.  
    *
    * This class only concerns API designers; ClientAgent registration with 
    * the server takes place transparently to end users.
    *
    * @see edu.uwlax.enmas.server.SimServer
    * @see edu.uwlax.enmas.client.ClientAgent
    */
  case object RegisterConfirmation

  /** Sent by an AgentProxy instance to the ClientAgent it is assigned to.
    *
    * This message should be handled (i.e. within a receive block) in concrete
    * subclasses of ClientAgent.
    *
    * @see edu.uwlax.enmas.server.AgentProxy
    * @see edu.uwlax.enmas.client.ClientAgent
    */
  case class Update(
    reward: Float,
    observation: State,
    actions: Set[Action]
  ) extends Message

  /** Sent by an AgentProxy instance to the ClientAgent it is assigned to.
    *
    * This messsage should be handled (i.e. within a receive block) in concrete
    * subclasses of ClientAgent.
    *
    * The ClientAgent recipient of a Decide message should invoke its 
    * takeAction method A.S.A.P.
    *
    * @see [[edu.uwlax.enmas.server.AgentProxy]]
    * @see [[edu.uwlax.enmas.client.ClientAgent]]
    */
  case object Decide extends Message

  /** Sent by a ClientAgent instance to the AgentProxy assigned to it.
    * The ClientAgent can simply invoke its takeAction method to automatically
    * reply with a TakeAction message. 
    *
    * @see [[edu.uwlax.enmas.server.AgentProxy]]
    * @see [[edu.uwlax.enmas.client.ClientAgent]]
    */
  case class TakeAction(action: Symbol) extends Message

  /** Sent by an AgentProxy instance to the ClientAgent it is assigned to.
    * This messsage should be handled (i.e. within a receive block) in concrete
    * subclasses of ClientAgent.
    *
    * The ClientAgent recipient of a TimeoutWarning message is advised that
    * a response was not received quickly enough by the server and that it was
    * assigned the null action for that simulation step.
    *
    * @see [[edu.uwlax.enmas.server.AgentProxy]]
    * @see [[edu.uwlax.enmas.client.ClientAgent]]
    */
  case object TimeoutWarning extends Message
}