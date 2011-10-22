package org.enmas.examples

import org.enmas.pomdp._

object Simple {
  
  object myModel extends POMDP (
    "Example", "Just for illustration",
    List ( AgentConstraint('A1, 2, 2) ),
    State.empty + ("time" -> 0),
    (_)  ⇒ Set('win, 'lose),
    (state, actions)  ⇒ {
      state.getAs[Int]("time") match {
        case Some(t)  ⇒ state + ("time" -> (t + 1))
        case _  ⇒ state
      }
    },
    (state, actions, statePrime)  ⇒ (agentType)  ⇒ {
      if (actions.foldLeft(true)( (a, b) => { a && b.action == 'win })) 1
      else 0
    },
    (state, actions, statePrime)  ⇒ (agentNumber, agentType)  ⇒ {
      state
    }   
  )

}