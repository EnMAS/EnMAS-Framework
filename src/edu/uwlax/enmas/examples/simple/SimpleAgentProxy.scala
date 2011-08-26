package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas._, edu.uwlax.enmas.server.AgentProxy,
  edu.uwlax.enmas.server.Mode._
import scala.actors._

class SimpleAgentProxy(
  actor: AbstractActor, 
  name: Symbol, 
  mode: Mode = SYNCHRONOUS
) extends AgentProxy(actor, name, mode) {

  // agent is an omnicient observer
  val observationFunction = (s: State) => s

  // agent always chooses between "win" and "lose"
  val actionsFunction = (s: State) => Set('win, 'lose)

  // simply checks for "win" or "lose" in set of last actions
  val rewardFunction: State => Float =
    (s: State) => s.getAs[Set[AgentCase]](POMDP.agentsKey) match {
      case Some(agents) => agents.headOption match {
        case Some(a) => if (a.action == 'win) 1f else -1f
        case _ => 0f
      }
      case _ => 0f
    }

}