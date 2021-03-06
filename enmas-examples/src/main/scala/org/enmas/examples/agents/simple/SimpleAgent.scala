package org.enmas.examples.agents.simple

import org.enmas.pomdp.{Agent, Action, State}
import scala.util.Random

class simpleAgent extends Agent {

  val random = new Random
  val winProbability = 0.7

  def name = "Simple Agent"

  def policy(observation: State, reward: Float): Action = {

    observation.getAs[Int]("time") map { 
      t => if (t % 1000 == 0) println(t.toString)
    }

    if (random.nextDouble < winProbability) Action("win")
    else Action("lose")
  }
}