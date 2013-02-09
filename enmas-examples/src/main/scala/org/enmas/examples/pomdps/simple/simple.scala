package org.enmas.examples.pomdp.simple

import org.enmas.pomdp._
import org.enmas.pomdp.State.Implicits._

case class SimplePOMDP() extends POMDP (

  name = "Example POMDP",

  description = "Just a simple POMDP",

  agentConstraints = List(
    AgentConstraint('TeamA, 1, Int.MaxValue)
  ),

  initialState = State("time" -> 0),

  actionsFunction = (_) => Set(
    Action("win"),
    Action("lose")
  ),

  transitionFunction = (state, actions) => {
    val t = state.getAs[Int]("time") getOrElse 0
    State("time" -> (t + 1))
  },

  rewardFunction = (state, actions, statePrime) => (aNum, aType) => {
    val allChoseWin = actions.foldLeft(true) {
      (result, a) => result && a.action == Action("win")
    }
    if (allChoseWin) 1
    else 0
  },

  observationFunction = (state, actions, statePrime) => (aNum, aType) => state
)
