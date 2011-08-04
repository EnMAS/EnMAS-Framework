package edu.uwlax.enmas.client

import edu.uwlax.enmas._, edu.uwlax.enmas.messages._

import scala.actors._, scala.actors.Actor._,
  scala.actors.Futures._, scala.actors.remote._,
  scala.actors.remote.TcpService._,
  scala.actors.remote.FreshNameCreator._,
  scala.actors.remote.RemoteActor._,
  java.net._

/** Represents an AI agent running either locally or on a remote host.
  * All AI clients must extend this class in order to interface with
  * the POMDP simulation.
  *
  * Once instantiated, concrete subclasses of ClientAgent start themselves.
  * There is no need to call start on them.
  *
  * The only element implementations must supply is the mainLoop method.
  * act simply calls init and then repeatedly invokes mainLoop. */
abstract class ClientAgent(server: AbstractActor) extends Actor {
  final val host = InetAddress.getLocalHost.getHostAddress
  final val port = generatePort
  val name = newName()
  start

  /** Registers this client with the server and prepares this client
    * to receive messages.  This method is called by act. */
  protected final def init = {
    println("ClientAgent: init()")
    alive(port)
    register(name, self)
    print("ClientAgent "+name+": registering with server... ")
    server ! Register(host, port, name)
    receive {
      case RegisterConfirmation => println("done.")
      case _ => throw new Exception("Client registration failed!")
    }
  }

  /** Calls init, then repeatedly calls mainLoop forever. */
  final override def act {
    init
    loop { mainLoop }
  }

  /** Fully defines the behavior of this agent.
    * mainLoop implementations should handle (i.e. in a receive block)
    * Update, Decide, and TimeoutWarning messages as defined in
    * the edu.uwlax.enmas.messages package.
    *
    * The receiving code for the Decide message should
    * include a call to takeAction. */
  protected def mainLoop

  /** Replies to the sender of the currently received message with a TakeAction message.
    * This method must be called from within a receive block.
    * This method should be called in response to the Decide message.
    * 
    * @param actionName The name of the action to take.  This String should be an element
    *   of the actions set received in the latest Update message.  Sending an invalid key
    *   here results in this agent choosing the null action for the simulation step.
    */
  final protected def takeAction(actionName: String) = reply{ TakeAction(actionName) }

  /** Consumes all pending messages.
    * The library should have implemented atomic receiveAndClear */
	protected final def clear: Unit = receiveWithin(0) {
		case TIMEOUT => ()
		case _ => clear
	}

}