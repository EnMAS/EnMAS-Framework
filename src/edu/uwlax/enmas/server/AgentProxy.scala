package edu.uwlax.enmas.server

import edu.uwlax.enmas._, edu.uwlax.enmas.server.SimServer._, 
  edu.uwlax.enmas.server.Mode._, edu.uwlax.enmas.messages._

import scala.actors._, scala.actors.Actor._, scala.actors.Futures._,
  scala.actors.remote._, scala.actors.remote.TcpService._,
  scala.actors.remote.FreshNameCreator._, scala.actors.remote.RemoteActor._,
  java.net._

/** Serves as a proxy for a client AI agent
  *
  * The client AI agent may be remote or local, depending on
  * the underlying AbstractActor member. */
abstract class AgentProxy(
  actor: AbstractActor,
  actorName: Symbol,
  mode: Mode = SYNCHRONOUS
) extends Agent {

  /** Provides consistency between calls to update and calls to action */
  private var actionSet = Set[Action]()

  /** Provides consistency between calls to update and calls to action */
  private var currentAction: Action = null

  val name = actorName
  val observationFunction: State => State
  val actionsFunction: State => Set[Action]
  val rewardFunction: State => Float

  /** Sends an [[edu.uwlax.enmas.messages.Update]] message to the underlying
    * {{AbstractActor}}. */
  final def update (
    observation: State,
    actions: Set[Action],
    reward: Float
  ): Unit = synchronized {
    actionSet = actions
    currentAction = null
    Communicator ! Update(reward, observation, actions)
  }

  /** Returns the associated agent client's chosen action
    * or possibly POMDP.NO_ACTION if the mode is ASYNCHRONOUS. */
  final override def action: Action = synchronized {
    if (currentAction == null) currentAction = Communicator !? FetchAction(mode) match {
      case TakeAction(a) => if (actionSet.contains(a)) a else POMDP.NO_ACTION
      case _ => POMDP.NO_ACTION
    }
    currentAction
  }

  /** Message wrapper for the internal Communicator.  The Communicator's
    * behavior differs depending on the value of the [[edu.uwlax.enmas.server.Mode]]
    * member. */
  private case class FetchAction(mode: Mode)

  /** Delegate object responsible for communicating with the underlying 
    * AbstractActor. */
  private final object Communicator extends DaemonActor {

    start  // essential!

    /** Overrides act to forward [[edu.uwlax.enmas.messages.Update]]
      * messages to the underlying AbstractActor.
      *
      * Also, in response to a FetchAction message, requests the
      * next action from the underlying AbstractActor by sending a
      * [[edu.uwlax.enmas.messages.Decide]] message and expecting a
      * [[edu.uwlax.enmas.messages.TakeAction]] message in return.
      *
      * If Mode member of the TakeAction message is ASYNCHRONOUS,
      * the underlying abstract actor has a finite amount of time (defined in
      * [[edu.uwlax.enmas.server.SimServer]]) to respond.  Any agent that
      * fails to respond in a timely fashion while in ASYNCHRONOUS mode
      * is automatically assigned POMDP.NO_ACTION (a do-nothing action)
      * for the current iteration. */
    override def act = {
      alive(generatePort)
      register(newName(), self)
      loop {
        react {

          case update: Update => synchronized {
            actor ! update
          }

          case FetchAction(ASYNCHRONOUS) => synchronized {
            try {
              actor !?(retryInterval, Decide) match {
                case Some(a: TakeAction) => reply { a }
                case None => {
                  actor ! TimeoutWarning
                  reply { TakeAction(null) }
                }
              }
            }
            catch {
              case e: Exception => {
                actor ! TimeoutWarning
                reply { TakeAction(null) }
              }
            }
          }

          case FetchAction(SYNCHRONOUS) => synchronized {
            actor !? Decide match {
              case a: TakeAction => reply { a }
              case _ => reply { TakeAction(null) }
            }
          }

        }
      }
    }
  }

}