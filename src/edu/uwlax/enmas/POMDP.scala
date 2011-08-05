package edu.uwlax.enmas

import edu.uwlax.enmas.server.AgentProxy
import scala.reflect.{Manifest, ClassManifest},
  ClassManifest.fromClass
import scala.actors.Future, scala.actors.Futures._

/** Thrown when an invalid State is detected.  At this point the recovery
  * behavior is not defined. */
case class StateException(msg:String) extends Exception(msg)

/** Represents a decentralized, partially observable Markov Decision Problem.
  * This class is the heart of the edu.uwlax.enmas package. */
class POMDP(initialState: State, arbiter: Set[State] => State) {

  /** In-order history of states. The initial state is the last element. */
  private var history = initialState :: Nil

  /** The list of agents is stored in the state, in (agentsKey, agents) */
  private final val agentsKey = "$AGENTS"

  /** Returns the current total in-order history of states in
    * which the initial state is the last in the list. */
  final def stateHistory(): List[State] = history

  /** Returns the List obtained by prepending the result of the transition
    * function to the old history.
    *
    * Most calling code will ignore the return value.  This method also
    * has the side effect of updating this object's history member. 
    *
    * First, the [[edu.uwlax.enmas.server.AgentProxy]] members of the agents
    * list are updated with an observation, a set of actions, and a reward.
    *
    * Next, the result of the transition function is prepended to the internal
    * state history. */
  final def iterate[A <: Agent](agents: Set[A]) = synchronized {
    // update clients with obs, reward, actions
    agents.foreach(
      _ match { case proxy: AgentProxy => {
        proxy.update(
          proxy.observation(history.head),
          proxy.actions(history.head),
          proxy.reward(history.head)
        )}})

    // prepend next state to history
    history +:= transition(history.head, agents)
  }

  /** Returns the result of applying the arbiter function to the set of
    * substates produced by each [[edu.uwlax.enmas.Agent]]'s chosen 
    * action function as applied to the supplied [[edu.uwlax.enmas.State]]. */
  private final def transition(state: State, agents: Set[_ <: Agent]) = {
//    val substates: Set[State] = agents.map(_.action(state))

    // dispatch all proxies to fetch action decisions
    val actionFutures = List[Future[Action]]()
    agents.map((a) => future{ a.action } :: actionFutures)

    // wait for results, apply actions to compute substates
    val substates: Set[State] = actionFutures.map(_()(state)).toSet

    // apply the arbiter function to resolve substates, completing the transition
    val resolvedState =
      if (substates.isEmpty) arbiter(Set(state))
      else arbiter(substates)

    // override the previous agent set, verify the new state against invariants
    verifyState(replaceAgents(resolvedState, agents))
  }

  /** Overrides a tuple to the state binding the String agentsKey to the given Set of Agents. */
  private final def replaceAgents[A <: Agent](state: State, agents: Set[A]) : State = {
    state + (agentsKey -> agents.map(_.toCaseClass))
  }

  /** Checks invariants pertaining to the State.  If any fail,
    * throws a StateException.  Otherwise returns the unmodified input State. */
  private final def verifyState(state: State): State = {
    state
  }

}

/** Companion object to the POMDP class.  Supplies static members. */
object POMDP {
  /** The Identity action:  does not modify the input State. */
  val NO_ACTION = (s: State) => s
}
