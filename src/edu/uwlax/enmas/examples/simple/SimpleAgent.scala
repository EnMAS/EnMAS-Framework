package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas.client.AgentClient, 
  edu.uwlax.enmas.messages._, 
  edu.uwlax.enmas.{Action, POMDP}
import scala.actors._, 
  scala.actors.Actor._, 
  scala.actors.remote.RemoteActor._

/** Most simplistic concrete subclass of ClientAgent.
  * Always takes the "first" action in set of available actions. */
class SimpleAgent(serverHost: String, serverPort: Int) 
    extends AgentClient(serverHost: String, serverPort: Int) {

  var action: Action = POMDP.NO_ACTION

  def mainLoop = react {
    case Update(reward, observation, actions) => {
      println(reward)
      action = actions.head
    }
    case Decide => takeAction(action)
    case TimeoutWarning => print("\t\tx")
  }

}