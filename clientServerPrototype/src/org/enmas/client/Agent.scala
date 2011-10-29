package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._

abstract class Agent() extends Client {

  private var replyChannel: UntypedChannel = null

  private var aNumber: Int = 0
  private var aType: AgentType = Symbol("")
  private var actionSet = Set[Action]()

  final def agentNumber = aNumber
  final def agentType = aType
  final def actions = actionSet

  final override def preStart = ()
  final override def preRestart(t: Throwable, o: Option[Any]) = ()
  final override def postRestart(t: Throwable) = ()
  final override def postStop = ()

  final def repliesTo(chan: UntypedChannel): Agent = { replyChannel = chan; this }

  def policy: PartialFunction[Any, Unit]

  final def receive = defaultMessageHandler

  private final def defaultMessageHandler: PartialFunction[Any, Unit] = {
    case ConfirmAgentRegistration(n, t, a)  ⇒ {
      aNumber = n
      aType = t
      actionSet = a
      become {policy orElse { case _  ⇒ () }}  // partial function chaining ftw...
    }
  }

  protected final def takeAction(action: Action) {
    if (actionSet contains action) replyChannel ! TakeAction(agentNumber, action)
    else replyChannel ! TakeAction(agentNumber, NO_ACTION)
  }
}