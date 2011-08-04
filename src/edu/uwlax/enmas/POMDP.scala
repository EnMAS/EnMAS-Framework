package edu.uwlax.enmas

import edu.uwlax.enmas.server.AgentProxy
import scala.reflect.{Manifest, ClassManifest},
  ClassManifest.fromClass

/** Thrown when an invalid State is detected.  At this point the recovery
  * behavior is not defined.
  */
case class StateException(msg:String) extends Exception(msg)

/** Represents  Central class in the edu.uwlax.enmas package.  
  * 
  */
class POMDP(initialState: State, arbiter: Set[State] => State) {

  /** 
    * 
    */
  private var history = initialState :: Nil

  /** 
    * 
    */
  private final val agentsKey = "$AGENTS"

  /** 
    * 
    */
  final def stateHistory(): List[State] = history

  /** 
    * 
    */
  final def iterate[A <: Agent](agents: Set[A]) = synchronized {
    // update clients with obs, reward, actions
    agents.foreach(
      _ match { case proxy: AgentProxy => {
        proxy.update(
          proxy.observation(history.last),
          proxy.actions(history.last),
          proxy.reward(history.last)
        )}})

    // get agent actions, compute next state
    history :+= transition(history.last, agents)
  }

  /** 
    * 
    */
  private final def transition(state: State, agents: Set[_ <: Agent]) = {
    val substates: Set[State] = agents.map(_.action(state))
    val resolvedState = 
      if (substates.isEmpty) arbiter(Set(state))
      else arbiter(substates)
    verifyState(replaceAgents(resolvedState, agents))
  }

  /** Adds a tuple to the state binding the String agentsKey to the given Set of Agents.
    * If the key exists in the 
    */
  private final def replaceAgents[A <: Agent](state: State, agents: Set[A]) : State = {
    state + (agentsKey -> agents.map(_.toCaseClass))
  }

  /** Checks known invariants pertaining to the State.  If any fail,
    * throws a StateException.  Otherwise returns the unmodified input State. */
  private final def verifyState(state: State): State = {
    state
  }

}

/** Companion object to the POMDP class.  Supplies static members. */
object POMDP {
  /** The Identity action:  does not modify the input State.
    */
  val NO_ACTION = (s: State) => s
}
