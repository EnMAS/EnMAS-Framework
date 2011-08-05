package edu.uwlax.enmas.server

import edu.uwlax.enmas._, edu.uwlax.enmas.messages._

import scala.actors._, scala.actors.Actor._, scala.actors.Futures._,
  scala.actors.remote._, scala.actors.remote.TcpService._,
  scala.actors.remote.FreshNameCreator._, scala.actors.remote.RemoteActor._,
  java.net._

  /** Determines which blocking mode the
    * server operates in. */
object Mode extends Enumeration {
  type Mode = Value
  val SYNCHRONOUS, ASYNCHRONOUS = Value
}

/** Manages the relationships between the POMDP representation,
  * the remote client agents, and the helper objects responsible
  * for communicating with the clients. */
class SimServer(
  pomdp: POMDP,
  agentBuilder: AgentProxy,
  port: Int = SimServer.defaultServerPort,
  serverName: Symbol = newName()
) {

  protected var agents: List[Agent] = Nil

  while(true) { try {
      Thread.sleep(SimServer.iterationInterval)
      agents ++= AgentRegistrar.getAndClearQueue
      pomdp.iterate(agents.toSet)
    }
    catch {
      case e: Exception => e.printStackTrace
    }
  }

  /** Delegate object that registers new [[edu.uwlax.enmas.client.ClientAgent]]s
    * with the system and dispatches responsibility for communicating with
    * them to a new instance of AgentProxy. */
  private object AgentRegistrar extends DaemonActor {
  	classLoader = getClass().getClassLoader() // hack!
    private var queue: List[AgentProxy] = Nil
    start

    override def act() = loop { try {
      alive(port)
      register(serverName, self)
      receive {
        case Register(host, port, clientName) => synchronized {
          print("AgentRegistrar: registering new agent "+clientName+"... ")
          queue ::= agentBuilder.build(select(Node(host, port), clientName), clientName)
          reply{
            RegisterConfirmation
          }
          println("done.")
        }
        case _ => ()
      }}
      catch {
        case e: Exception => e.printStackTrace
        case _ => println("AgentRegistrar: caught something else")
      }
    }

    def getAndClearQueue = synchronized {
      val result = queue
      queue = Nil
      result
    }
  }

}

/** Companion object for [[edu.uwlax.enmas.server.SimServer]].  Provides static values. */
object SimServer {

  /** Port for the server to listen on for registration messages from clients */
  final val defaultServerPort = 9700

  /** Number of ms to pause before each iteration of the underlying POMDP */
  final val iterationInterval = 10

  /** Number of ms to wait before rechecking agent responses in SYNCHRONOUS mode.
    * This value is also the one-time grace period in ASYNCHRONOUS mode. */
  final val retryInterval = 90
}