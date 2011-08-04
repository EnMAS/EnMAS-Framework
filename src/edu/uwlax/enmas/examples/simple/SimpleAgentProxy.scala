package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas._, edu.uwlax.enmas.server.AgentProxy,
  edu.uwlax.enmas.server.Mode._
import scala.actors._

/** 
  * 
  */
class SimpleAgentProxy(
  actor: AbstractActor, 
  name: Symbol, 
  mode: Mode = SYNCHRONOUS
) extends AgentProxy(actor, name, mode) {

  // agent is an omnicient observer
  val observation = (s: State) => s

  // agent always chooses between "win" and "lose"
  val actions: State => Map[String, State => State] = {
    val win = ("win", (s: State) => s + ("lastAction" -> "win"))
    val lose = ("lose", (s: State) => s + ("lastAction" -> "win"))
    (s: State) => Map() + lose + win
  }

  // simply checks for "win" or "lose" as last action
  val reward: State => Float =
    (s: State) => s.find((t:(String, Any)) => t._1 == "lastAction") match {
      case Some((_, "win")) => 1f
      case Some((_, "lose")) => -1f
      case _ => 0f
    }

  // the server delegates to this function to create new proxies
  def build(actor: AbstractActor, name: Symbol): AgentProxy = 
    new SimpleAgentProxy(actor, name, ASYNCHRONOUS)
}