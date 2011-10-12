package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._,
       scala.collection.immutable._

class Server(
  model: POMDP,
  port: Int,
  logger: Logger
) extends Actor {

  var state = model.initialState
  var clientManagers = Set[ClientManagerRef]()
  var agents = Set[AgentRef]()
  var messageQueue = Map[ClientManagerRef, List[Message]]()
  var pendingActions = Map[AgentRef, Action]()


  private def registerAgent(cm: ClientManagerRef, reg: RegisterAgent): ClientMessage = {
    val newAgent = AgentRef(cm, reg.agentRef, agents.size+1, reg.agentType)
    var newAgentSet = agents + newAgent
    if (model accomodatesAgents { newAgentSet.toList map {_.agentType} }) {
      agents = newAgentSet
      ConfirmAgentRegistration(
        reg.agentRef,
        newAgent.agentNumber,
        newAgent.agentType,
        model.actionsFunction(reg.agentType)
      )
    }
    else DenyAgentRegistration(reg.agentRef)
  }


  private def iterate(actions: JointAction): State = {
    val reward = model.rewardFunction(state, actions)
    val observation = model.observationFunction(state, actions)
    clientManagers map { cm => {
      messageQueue += cm -> { for (a <- agents.toList.filter(_.clientManagerRef == cm))
        yield UpdateAgent(a.ref, observation(a.agentType), reward(a.agentType))}
    }}
    model.transitionFunction(state, actions)
  }


  private def dispatchMessages(): Unit = {
    clientManagers map { cm => {
        messageQueue.get(cm) map {cm.ref ! MessageBundle(_) }}}
  }


  private def getAgent(ref: ActorRef) = agents.find( _.ref == ref )

  private def getCM(ref: ActorRef) = clientManagers.find( _.ref == ref )


  def receive = {
    case m: RegisterAgent => self.sender map { source => {
          getCM(source) map { cm => self.reply(registerAgent(cm, m)) }}}

    case TakeAction(action) => self.sender map { source => {
        getAgent(source) map { agent => pendingActions += (agent -> action) }}}

    case _ => ()
  }

}