package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas.client.ClientAgent, edu.uwlax.enmas.messages._
import scala.actors._, scala.actors.Actor._, scala.actors.remote.RemoteActor._

/** Most simplistic concrete subclass of ClientAgent possible.
  * Always takes the "first" action in set of available actions. */
class SimpleAgent(server: AbstractActor) extends ClientAgent(server: AbstractActor) {
  var actionName: String = null

  def mainLoop = receive {
    case Update(reward, observation, actions) => {
        actionName = actions.head
//        println("SimpleAgent: received reward: "+reward+", decided to take action: "+actionName)
    }
    case Decide => {
      takeAction(actionName)
      print(".")
    }
    case TimeoutWarning => print("x")
  }

}