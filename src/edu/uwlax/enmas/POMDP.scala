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
class POMDP(initialState: State, transitionFunction: (State, Set[AgentCase]) => State) {

  /** In-order history of states. The initial state is the last element. */
  private var history = initialState :: Nil

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
    val state = history.head

    // update clients with observation, reward, actions
    agents.foreach(
      _ match { case agent: AgentProxy => {
        agent.update(
          agent.observationFunction(state),
          agent.actionsFunction(state),
          agent.rewardFunction(state)
        )}})

    // dispatch all proxies to fetch action decisions
    var actionFutures = List[(A, Future[Action])]()
    agents.map((a) => actionFutures +:= (a, future{ a.action }))

    // resolve actions, flatten all agents into case class representations
    val agentCases = actionFutures.map(
      af => AgentCase(
        af._1.name,
        af._1.observationFunction(state),
        af._1.actionsFunction(state),
        af._1.rewardFunction(state),
        af._2()
      )).toSet

    // invokes the transition function, override the previous agent set,
    // prepend result state to the history
    history +:= transitionFunction(state, agentCases) + (POMDP.agentsKey -> agentCases)
  }

}

/** Companion object to the POMDP class. */
object POMDP {
  /** The Identity action:  does not modify the input State. */
  val NO_ACTION: Action = 'NO_ACTION
  /** The list of agents is stored in the state, in (agentsKey, agents) */
  val agentsKey = "AGENTS"
}
