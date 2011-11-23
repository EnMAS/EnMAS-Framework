package org.enmas.examples

import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

object Simple {

  val simpleModel = POMDP (

    name = "Example POMDP Model",

    description = "Just for illustration",

    agentConstraints = List ( AgentConstraint('A1, 1, 1), AgentConstraint('A2, 1, 1)),

    initialState = State.empty + ("time"  → 0),

    actionsFunction = (_)  ⇒ Set('win, 'lose),

    transitionFunction = (state, _)  ⇒ {
      state.getAs[Int]("time") match {
        case Some(t)  ⇒ List((state + ("time"  → (t+1)), 1))
        case _  ⇒ List((state, 1))
      }
    },

    rewardFunction = (state, actions, _)  ⇒ (_)  ⇒ {
      if (actions.foldLeft(true)( (a, b)  ⇒ { a && b.action == 'win })) 1
      else 0
    },

    observationFunction = (state, _, _)  ⇒ (_, _)  ⇒ state
  )

  class simpleAgent extends Agent {
    def handleError(error: Throwable) {}
    def handleUpdate(observation: Observation, reward: Float): Action = {
      observation.getAs[Int]("time") map { t  ⇒ if (t % 1000 == 0) { println(t.toString) }}
      'win
    }
  }

}