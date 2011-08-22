package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas.client.ClientAgent, edu.uwlax.enmas.messages._
import scala.actors._, scala.actors.Actor._, scala.actors.remote.RemoteActor._

/** Most simplistic concrete subclass of ClientAgent.
  * Always takes the "first" action in set of available actions. */
class SimpleAgent(server: AbstractActor) extends ClientAgent(server: AbstractActor) {

  var action: String = null

  def mainLoop = react {
    case Update(reward, observation, actions) => action = actions.head
    case Decide => takeAction(action)
    case TimeoutWarning => print("x")
  }

}