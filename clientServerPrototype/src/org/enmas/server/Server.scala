package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._, org.enmas.util.EncryptionUtils._,
       akka.actor._, akka.actor.Actor._,
       scala.util._, scala.collection.immutable._,
       java.security._

class Server(model: POMDP, localhost: String, port: Int, service: String) extends Actor {
  private val keyPair = genKeyPair
  private var state = model.initialState
  private var iterationOrdinality = 0L
  private var clientManagers = Set[ClientManagerRef]()
  private var iterationSubscribers = Set[ClientManagerRef]()
  private var agents = Set[AgentRef]()
  private var messageQueue = Map[ClientManagerRef, List[AgentMessage]]()
  private var pendingActions = List[AgentAction]()
  
  private var iterating = false

  /** Creates a new ClientManagerRef object for the new host and stores it in
    * the local list of client managers.  The ClientManagerRef contains a
    * unique id for this host, as well as a newly generated symmetric key.
    */
  private def registerHost(
      service: String,
      hostname: String,
      port: Int,
      clientPublicKey: PublicKey
  ): Message = {
    val actorRef = remote.actorFor(service, hostname, port)
    val cmID = clientManagers.size + 1
    val symKey = genSymKey
    clientManagers += ClientManagerRef(cmID, actorRef, clientPublicKey, symKey)
    ConfirmHostRegistration(cmID, keyPair.getPublic, symKey.getEncoded)  // TODO: check for approval, encrypt!
  }

  /** Creates a new AgentRef for the new Agent.  Replies with a
    * ConfirmAgentRegistration message if the POMDP model accomodates the new
    * Agent, and with a DenyAgentRegistration message otherwise.
    */
  private def registerAgent(clientManagerID: Int, agentType: AgentType): Message = {
    val a = AgentRef(clientManagerID, agents.size+1, agentType)
    var newAgentSet = agents + a
    if (model accomodatesAgents { newAgentSet.toList map {_.agentType} }) {
      agents = newAgentSet
      self ! TakeAction(a.agentNumber, NO_ACTION)
      ConfirmAgentRegistration(a.agentNumber, a.agentType, model.actionsFunction(agentType))
    }
    else DenyAgentRegistration
  }

  /** Places a new AgentAction into the local collection of pending actions,
    * unless it already contains an action for the supplied agent number.
    *
    * Furthermore, if the current agents set satisfies the POMDP model and all
    * agents have a pending action, iterates the simulation by calling the
    * iterate method.
    */
  private def takeAction(agentNumber: Int, action: Action) {
    if ((pendingActions filter { _.agentNumber == agentNumber }).isEmpty ) {
      getAgent(agentNumber) map {
        a  ⇒ pendingActions ::= AgentAction(a.agentNumber, a.agentType, action)
      }
      if (
        model.isSatisfiedByAgents(agents.toList map {_.agentType}) &&
        pendingActions.length == agents.size
      ) { state = iterate(state, pendingActions) }
    }
  }

  /** Iterates the simulation and returns the resulting next state.
    *
    * 1) Passes the supplied state and actions to the POMDP transitionFunction
    * 2) Uses the resulting probability distribution to select the next state
    * 3) Dispatches UpdateAgent messages to client managers
    */
  private def iterate(state: State, actions: JointAction): State = {
    iterating = true
    try {
      val statePrime = selectState(model.transitionFunction(state, actions))
      val reward = model.rewardFunction(state, actions, statePrime)
      val observation = model.observationFunction(state, actions, statePrime)
      var observations = Set[(AgentRef, Observation)]()
      var rewards = Set[(AgentRef, Float)]()

      clientManagers map { cm  ⇒ {
        val theseAgents = agents.toList.filter(_.clientManagerID == cm.id)
        for (a  ← theseAgents) {
          observations += (a  → observation(a.agentNumber, a.agentType))
          rewards += (a  → reward(a.agentType))
        }
        messageQueue += cm  → { for (a  ← theseAgents)
          yield UpdateAgent(
            a.agentNumber,
            observation(a.agentNumber, a.agentType),
            reward(a.agentType))}
      }}

      pendingActions = pendingActions take 0 // clears pending actions
      dispatchMessages // sends bundled agent updates to client managers
      val iteration = POMDPIteration(iterationOrdinality, observations, rewards, actions, state)
      // TODO: send iteration to POMDPIteration subscribers
      iterationOrdinality += 1;
      statePrime
    }
    catch {
      case t: Throwable  ⇒ clientManagers map { cm  ⇒ cm.channel ! t }
      state
    }
  }

  /** Returns one State according to a normalized probability distribution
    * represented as a List of (State, Int) tuples.
    */
  private def selectState(all: List[(State, Int)]) = {
    def stateAt(possible: List[(State, Int)], scalar: Int): State =
      if (scalar <= 0) possible.head._1 else stateAt(possible.tail, scalar - possible.head._2)

    val possible = all filter { _._2 > 0 }
    if (possible.size > 0) {
      val totalWeight = possible.foldLeft(0)((a, b)  ⇒ a + b._2)
      val randomScalar = (new Random) nextInt totalWeight
      stateAt(possible, randomScalar)
    }
    else state
  }


  /** Sends a MessageBundle object to each ClientManager in the list of hosts.
    * The MessageBundle objects are culled from the outbound message queue.
    * This method also resets the outbound message queue.
    */
  private def dispatchMessages: Unit = {
    clientManagers map { cm  ⇒ { messageQueue.get(cm) map { cm.channel ! MessageBundle(_) }}}
    messageQueue = messageQueue.empty
  }


  /** Returns either Some[AgentRef] containing an AgentRef with the
    * supplied agentNumber, or None if no such object exists in the local
    * list of connected agents.
    */
  private def getAgent(agentNumber: Int) = agents.find( _.agentNumber == agentNumber )


  /** Returns either Some[ClientManager] containing a ClientManager with the
    * supplied id, or None if no such object exists in the local
    * list of connected client managers.
    */
  private def getClientManager(id: Int) = clientManagers.find( _.id == id )


  /** Handles RegisterHost, RegisterAgent, and TakeAction messages by delegating to
    * the appropriate handler method.
    */
  def receive = {
    case Discovery  ⇒ {
      self.channel ! DiscoveryReply(localhost, service, port, model.name, iterating)
    }
    case reg: RegisterHost  ⇒ {
      self.channel ! registerHost(
        reg.service, reg.hostname, reg.port, reg.clientPublicKey
      )
    }
    case reg: RegisterAgent  ⇒ getClientManager(reg.clientManagerID) map {
      cm  ⇒ self.channel ! registerAgent(cm.id, reg.agentType)
    }
    case TakeAction(agentNumber, action)  ⇒ takeAction(agentNumber, action)
    case _  ⇒ ()
  }
}