package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._

abstract class Agent extends Client {

  private var aType: AgentType = Symbol("")
  private var actionSet = Set[Action]()

  def agentType = aType
  def actions = actionSet

  def policy: PartialFunction[Any, Unit]

  def receive = {
    case ConfirmAgentRegistration(_, t, a) => {
      aType = t
      actionSet = a
      become(policy)
    }
    case DenyAgentRegistration => self ! PoisonPill
    case _ => ()
  }

  protected final def takeAction(action: Action) = {
    self.reply(TakeAction(action))
  }
}
