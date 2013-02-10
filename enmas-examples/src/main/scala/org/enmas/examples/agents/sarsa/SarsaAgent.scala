package org.enmas.examples.agents.sarsa

import org.enmas.pomdp.{Agent, Action, State}
import scala.util.Random

/**
  * Implementes the SARSA reinforcement learning algorithm, a modified
  * version of Q-Learning.
  */
class SarsaAgent extends Agent {

  val random = new scala.util.Random
  val randomFactor = 0.1f // this agent acts randomly 10% of the time
  val alpha = 0.9f
  val gamma = 0.9f
  var qTable = Map[(State, Action), Float]()
  var lastSA = (State(), Action())

  def name = "Sarsa Agent"

  def policy(observation: State, reward: Float): Action = {

    val options: Seq[(Action, Float)] =
      for (a <- actions.toSeq) yield (a, expectedReward(observation, a))

    lazy val bestGuess = options.sortWith((o1, o2) => o1._2 > o2._2).head._1

    val decision = if (random.nextDouble < randomFactor) randomAction()
                   else bestGuess

    val currentSA = (observation, decision)

    val currentExpected = expectedReward(observation, decision)

    val lastExpected = expectedReward(lastSA._1, lastSA._2)

    val newQValue = lastExpected + alpha * (
      reward + gamma * currentExpected - lastExpected
    )

    qTable += (lastSA -> newQValue) // update the Q-Table
    lastSA = currentSA

    decision
  }

  def expectedReward(state: State, action: Action): Float = 
    qTable.get(state, action) getOrElse 0

  def randomAction(): Action = actions.toSeq(random nextInt actions.size)
}