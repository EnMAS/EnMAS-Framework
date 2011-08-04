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
  * the underlying AbstractActor member.
  */
abstract class AgentProxy(
  actor: AbstractActor,
  actorName: Symbol,
  var mode: Mode = SYNCHRONOUS
) extends Agent {

  /** 
    * 
    */
  private var actionMap = Map[String, State => State]()

  /** 
    * 
    */
  private var currentAction: Action = null
  
  val name = actorName
  val observation: State => State                    // observation
  val actions: State => Map[String, State => State]  // actions
  val reward: State => Float                         // reward

  /** 
    * 
    */
  def build(actor: AbstractActor, name: Symbol): AgentProxy

  /** 
    * 
    */
  final def update (
    observation: State,
    actions: Map[String, Action],
    reward: Float
  ): Unit = synchronized {
    actionMap = actions
    currentAction = null
    Communicator ! Update(reward, observation, (new HashSet[String]()).union(actions.keySet))
  }

  /** Returns the associated agent client's chosen action
    * or possibly POMDP.NO_ACTION if the mode is ASYNCHRONOUS. */
  final override def action: Action = synchronized {
    if (currentAction == null) currentAction = Communicator !? FetchAction(mode) match {
      case TakeAction(key) => actionMap.getOrElse(key, POMDP.NO_ACTION)
      case _ => POMDP.NO_ACTION
    }
    currentAction
  }

  /** 
    * 
    */
  private case class FetchAction(mode: Mode)

  /** 
    * 
    */
  private final object Communicator extends DaemonActor {

    start  // essential!

    /** 
      * 
      */
    override def act = {
      alive(generatePort)
      register(newName(), self)
      loop {
        react {

          //
          case update: Update => synchronized {
            actor ! update
          }

          //
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

          // 
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