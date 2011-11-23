package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._

abstract class Agent() extends Client {
  private var replyChannel: UntypedChannel = null
  private var aNumber: Int = 0
  private var aType: AgentType = Symbol("")
  private var actionSet = Set[Action]()

  /** For Java API */
  val NO_ACTION = Symbol("")

  /** Returns the unique (per-server instance) identifier for this agent.
    */
  final def agentNumber = aNumber

  /** Returns the type of this agent, as defined by the POMDP model.
    */
  final def agentType = aType

  /** Returns the set of actions that this agent may choose to take.
    */
  final def actions = actionSet

  /** This method is overridden and made final to sandbox user code.
    */
  final override def preStart = ()

  /** This method is overridden and made final to sandbox user code.
    */
  final override def preRestart(t: Throwable, o: Option[Any]) = ()

  /** This method is overridden and made final to sandbox user code.
    */
  final override def postRestart(t: Throwable) = ()

  /** This method is overridden and made final to sandbox user code.
    */
  final override def postStop = ()

  /** Returns this instance, after setting the reply channel.  The
    * return value is to promote method chaining at the call site,
    * as in:
    *
    * {{{
    * val myAgent = actorOf(new MyAgent repliesTo self)
    * }}}
    */
  final def repliesTo(chan: UntypedChannel): Agent = { replyChannel = chan; this }

  /** Defines this agent's behavior.  This abstract method is the only one
    * that user code must implement.  To make sense, the policy should handle
    * UpdateAgent messages and call takeAction like so:
    *
    * {{{
    * def policy = { case UpdateAgent(_, observation, reward)  ⇒ {
    *   // learn, decide
    *   takeAction(decision)
    * }}
    * }}}
    */
  def policy: PartialFunction[Any, Unit] = {
    case t: Throwable  ⇒ handleError(t)
    case UpdateAgent(_, observation, reward)  ⇒ {
      try {
        takeAction(handleUpdate(observation, reward))
      }
      catch {
        case t: Throwable  ⇒ {
          self ! t
          replyChannel ! ClientError(t)
        }
      }
    }
  }

  def handleUpdate(observation: Observation, reward: Float): Action

  def handleError(error: Throwable): Unit

  /** Initially defaultMessageHandler.
    */
  final def receive = defaultMessageHandler

  /** Handles only the ConfirmAgentRegistration message.  The last step in
    * handling the confirmation is to redefine the receive behavior to include
    * the policy from the implementation.
    */
  private final def defaultMessageHandler: PartialFunction[Any, Unit] = {
    case ConfirmAgentRegistration(n, t, a)  ⇒ {
      println("Agent Initialized!")
      aNumber = n
      aType = t
      actionSet = a
      become {policy orElse { case _  ⇒ () }}  // partial function chaining ftw...
    }
  }

  /** Replies to the replyChannel (a local client manager) with a TakeAction
    * message indicating that this agent wants to take the supplied action.
    *
    * If the message passes security checks, it is forwarded to the server.
    */
  protected final def takeAction(action: Action) {
    if (actionSet contains action) replyChannel ! TakeAction(agentNumber, action)
    else replyChannel ! TakeAction(agentNumber, NO_ACTION)
  }
}