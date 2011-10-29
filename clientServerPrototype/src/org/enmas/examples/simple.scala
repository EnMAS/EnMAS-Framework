package org.enmas.examples

import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

object Simple {  

  val myModel = POMDP (

    name = "Example POMDP Model",

    description = "Just for illustration",

    agentConstraints = List ( AgentConstraint('A1, 1, 1) ),

    initialState = State.empty + ("time"  → 0),

    actionsFunction = (_)  ⇒ Set('win, 'lose),

    transitionFunction = (state, _)  ⇒ {
      state.getAs[Int]("time") match {
        case Some(t)  ⇒ state + ("time"  → (t + 1))
        case _  ⇒ state
      }
    },

    rewardFunction = (state, actions, _)  ⇒ (_)  ⇒ {
      if (actions.foldLeft(true)( (a, b)  ⇒ { a && b.action == 'win })) 1
      else 0
    },

    observationFunction = (state, _, _)  ⇒ (_, _)  ⇒ state
  )

  class myAgent extends Agent {
    def policy = { case u: UpdateAgent  ⇒ {
      u.observation.getAs[Int]("time") map { t  ⇒ if (t % 1000 == 0) { println(t.toString) }}
      takeAction( 'win ) }}
  }

}