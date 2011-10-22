package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._

abstract class Agent extends Client {

  private var aNumber: Int = 0
  private var aType: AgentType = Symbol("")
  private var actionSet = Set[Action]()

  final def agentNumber = aNumber
  final def agentType = aType
  final def actions = actionSet

  def policy: PartialFunction[Any, Unit]

  private final def defaultMessageHandler: PartialFunction[Any, Unit] = {
    case ConfirmAgentRegistration(_, n, t, a)  ⇒ {
      aNumber = n
      aType = t
      actionSet = a
    }
    case DenyAgentRegistration  ⇒ self ! PoisonPill
  }

  // partial function chaining ftw...
  def receive = defaultMessageHandler orElse policy orElse { case _ => () }

  protected final def takeAction(action: Action) = {
    self.reply(TakeAction(action))
  }
}