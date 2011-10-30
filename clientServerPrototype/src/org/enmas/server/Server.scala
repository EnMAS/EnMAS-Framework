package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._, org.enmas.util.EncryptionUtils._,
       akka.actor._, akka.actor.Actor._,
       scala.util._, scala.collection.immutable._,
       java.security._

class Server(model: POMDP, port: Int, logger: Logger) extends Actor {

  private val keyPair = genKeyPair
  private var state = model.initialState
  private var clientManagers = Set[ClientManagerRef]()
  private var agents = Set[AgentRef]()
  private var messageQueue = Map[ClientManagerRef, List[AgentMessage]]()
  private var pendingActions = List[AgentAction]()


  /** Handler for RegisterHost messages.
    * 
    * Creates a new ClientManagerRef object for the new host and stores it in the local
    * list of client managers.  The ClientManagerRef contains a unique id for this
    * host, as well as a newly generated symmetric key.
    */
  private def registerHost(
      service: String,
      hostname: String,
      port: Int,
      clientPublicKey: PublicKey
  ): Message = {
    val actorRef = remote.actorFor(service, hostname, port)
    val cmID = clientManagers.size + 1
    val symKey = createSymKey
    clientManagers += ClientManagerRef(cmID, actorRef, clientPublicKey, symKey)
    ConfirmHostRegistration(cmID, keyPair.getPublic, symKey.getEncoded)  // TODO: check for approval, encrypt!
  }


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


  private def takeAction(agentNumber: Int, action: Action) =
    if ((pendingActions filter { _.agentNumber == agentNumber }).isEmpty ) {
      getAgent(agentNumber) map { 
        a  ⇒ pendingActions ::= AgentAction(a.agentNumber, a.agentType, action)
      }
      if (
        model.isSatisfiedByAgents(agents.toList map {_.agentType}) &&
        pendingActions.length == agents.size
      ) { state = iterate(pendingActions) }
    }


  private def iterate(actions: JointAction): State = {
    val statePrime = selectState(model.transitionFunction(state, actions))
    val reward = model.rewardFunction(state, actions, statePrime)
    val observation = model.observationFunction(state, actions, statePrime)
    clientManagers map { cm  ⇒ {
      messageQueue += cm  → { for (a  ← agents.toList.filter(_.clientManagerID == cm.id))
        yield UpdateAgent(
          a.agentNumber,
          observation(a.agentNumber, a.agentType),
          reward(a.agentType))}
    }}
    pendingActions = pendingActions take 0
    dispatchMessages
    statePrime
  }
  
  
  private def selectState(all: List[(State, Int)]) = {
    def nthState(possible: List[(State, Int)], scalar: Int): State =
      if (scalar <= 0) possible.head._1 else nthState(possible.tail, scalar - possible.head._2)

    val possible = all filter { _._2 > 0 }
    val totalWeight = possible.foldLeft(0)((a, b)  ⇒ a + b._2)
    val randomScalar = (new Random) nextInt totalWeight
    nthState(possible, randomScalar)
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
  private def getCM(id: Int) = clientManagers.find( _.id == id )


  /** Handles RegisterHost, RegisterAgent, and TakeAction messages by delegating to
    * the appropriate handler method.
    */
  def receive = {
    case m: RegisterHost  ⇒ {
      val source = self.channel
      source ! registerHost(m.service, m.hostname, m.port, m.clientPublicKey)}

    case m: RegisterAgent  ⇒ getCM(m.clientManagerID) map {
      cm  ⇒ self.channel ! registerAgent(cm.id, m.agentType)}

    case TakeAction(agentNumber, action)  ⇒ takeAction(agentNumber, action)

    case _  ⇒ ()
  }
}