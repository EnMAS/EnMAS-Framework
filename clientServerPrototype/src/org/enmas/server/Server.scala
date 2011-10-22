package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._, org.enmas.util.EncryptionUtils._,
       akka.actor._,
       scala.collection.immutable._,
       java.security._

class Server(
  model: POMDP,
  port: Int,
  logger: Logger
) extends Actor {

  println("new Server")

  private val keyPair = genKeyPair

  var state = model.initialState
  var clientManagers = Set[ClientManagerRef]()
  var agents = Set[AgentRef]()
  var messageQueue = Map[ClientManagerRef, List[Message]]()
  var pendingActions = Map[AgentRef, Action]()


  private def registerHost(
    source: UntypedChannel,
    hostname: String,
    clientPublicKey: PublicKey
  ): Message = {
    clientManagers += ClientManagerRef(source, hostname, clientPublicKey, false)
    ConfirmHostRegistration(keyPair.getPublic, "lame-shared-key") // TODO: gen sym key
  }


  private def registerAgent(
      cm: ClientManagerRef,
      source: UntypedChannel,
      reg: RegisterAgent
  ): ClientMessage = {
    val newAgent = AgentRef(cm, source, agents.size+1, reg.agentType)
    var newAgentSet = agents + newAgent
    if (model accomodatesAgents { newAgentSet.toList map {_.agentType} }) {
      agents = newAgentSet
      ConfirmAgentRegistration(
        source,
        newAgent.agentNumber,
        newAgent.agentType,
        model.actionsFunction(reg.agentType)
      )
    }
    else DenyAgentRegistration(source)
  }


  private def iterate(actions: JointAction): State = {
    val statePrime = model.transitionFunction(state, actions)
    val reward = model.rewardFunction(state, actions, statePrime)
    val observation = model.observationFunction(state, actions, statePrime)
    clientManagers map { cm  ⇒ {
      messageQueue += cm  → { for (a  ← agents.toList.filter(_.clientManagerRef == cm))
        yield UpdateAgent(
          a.channel,
          observation(a.agentNumber, a.agentType),
          reward(a.agentType))}
    }}
    statePrime
  }


  private def dispatchMessages(): Unit = {
    clientManagers map { cm  ⇒ {
        messageQueue.get(cm) map {cm.channel ! MessageBundle(_) }}}
  }


  private def getAgent(c: UntypedChannel) = agents.find( _.channel == c )

  private def getCM(c: UntypedChannel) = clientManagers.find( _.channel == c )


  def receive = {
    
    case m: RegisterHost  ⇒ {
        val source = self.channel
        source ! registerHost(source, m.hostname, m.clientPublicKey)
    }

    case m: RegisterAgent  ⇒ {
      val source = self.channel
      getCM(source) map { cm  ⇒ source ! registerAgent(cm, source, m) }
    }

    case TakeAction(action)  ⇒ {
      val source = self.channel
      getAgent(source) map { agent  ⇒ pendingActions += (agent  → action) }
    }

    case m: AnyRef  ⇒ println("Received something else: "+m.getClass.getName+"\n"+m)

    case _  ⇒ ()
  }

}