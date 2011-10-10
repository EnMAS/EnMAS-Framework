package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor.Actor

class Server(
  model: POMDP,
  port: Int,
  logger: Logger
) extends Actor {

  var state = model.initialState
  val clientManagers = List[ClientManagerRef]()
  var messageQueue = Map[ClientManagerRef, List[Message]]()

  private def registerAgent(registration: RegisterAgent): ClientMessage = { // TODO: fix
    ConfirmAgentRegistration(registration.agentRef, registration.agentType)
  }
  
  private def iterate(actions: Set[(AgentRef, Action)]): State = { state } // TODO: fix
  
  private def sendActions(agentRef: AgentRef, actions: Set[Action]): Unit = {} // TODO: fix

  private def sendObservation(agentRef: AgentRef, observation: State): Unit = {} // TODO: fix

  private def sendReward(agentRef: AgentRef, reward: Float): Unit = {} // TODO: fix

  def dispatchMessages(): Unit = {
    // send out queued messages
    clientManagers map { cm => { messageQueue.get(cm) match {
      case Some(l) => cm.ref ! MessageBundle(l)
      case None => ()
    }}}
    // reset the message queue
    messageQueue = clientManagers map { cm => (cm, List[Message]()) } toMap
  }

  def receive = {
    case m: RegisterAgent => registerAgent(m)
    case _ => ()
  }

}