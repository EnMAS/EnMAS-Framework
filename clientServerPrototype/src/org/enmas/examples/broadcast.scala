package org.enmas.examples

import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

object Broadcast {  

  val broadcastProblem = POMDP (

    name = "2 Agent Broadcast Problem",
    description = "description to come later...",

    agentConstraints = List ( AgentConstraint('A1, 2, 2)),

    initialState = State.empty + ("1"  → false) + ("2"  → false),

    actionsFunction = (_)  ⇒ Set('send, 'wait),

    transitionFunction = (state, actions)  ⇒ {
      val m1 = state.getAs[Boolean]("1") match { case Some(true)  ⇒ true; case _  ⇒ false }
      val m2 = state.getAs[Boolean]("2") match { case Some(true)  ⇒ true; case _  ⇒ false }
      val a1 = actions contains { a: AgentAction  ⇒ a.agentNumber == 1 && a.action == 'send }
      val a2 = actions contains { a: AgentAction  ⇒ a.agentNumber == 2 && a.action == 'send }

      val allClearDistribution = List(
        (State.empty + ("1"  → true) + ("2"  → true), 9),
        (State.empty + ("1"  → true) + ("2"  → false), 81),
        (State.empty + ("1"  → false) + ("2"  → true), 1),
        (State.empty + ("1"  → false) + ("2"  → false), 9)
      )

      if (a1) { // 1 sends
        if (a2) { // 2 sends
          if (m1 && m2) List((state, 1))
          else allClearDistribution
        }
        else { // 2 waits
          if (m1 && m2 || !m1 && m2) List(
            (State.empty + ("1"  → true) + ("2"  → true), 9),
            (State.empty + ("1"  → false) + ("2"  → true), 1)
          )
          else allClearDistribution
        }
      }
      else { // 1 waits
        if (a2) { // 2 sends
          if (m1 && m2 || m1 && !m2) List(
            (State.empty + ("1"  → true) + ("2"  → true), 1),
            (State.empty + ("1"  → false) + ("2"  → true), 9)
          )
          else allClearDistribution
        }
        else { // 2 waits
          if (m1 && m2) List((state, 1))
          else if (m1 && !m2) List(
            (State.empty + ("1"  → true) + ("2"  → true), 1),
            (State.empty + ("1"  → true) + ("2"  → false), 9)
          )
          else if (!m1 && m2) List(
            (State.empty + ("1"  → true) + ("2"  → true), 9),
            (State.empty + ("1"  → false) + ("2"  → true), 1)
          )
          else allClearDistribution
        }
      }
      
    },

    rewardFunction = (s, actions, sPrime)  ⇒ (_)  ⇒ {
      val sendActions = actions filter {_.action == 'send}
      if (sendActions.size == 1) {
        val a = sendActions.head
        s.getAs[Boolean](a.agentNumber.toString) match {
          case Some(true)  ⇒ 1
          case _  ⇒ 0
        }
      }
      else if (sendActions.size == 2) {
        val test = actions filter { a: AgentAction  ⇒ {
          s.getAs[Boolean](a.agentNumber.toString) match {
            case Some(true)  ⇒ true
            case _  ⇒ false
          }
        }}
        if (test.size == 1) 1 else 0
      }
      else 0
    },

    observationFunction = (s, actions, sPrime)  ⇒ (aNum, aType)  ⇒ {
      val hasMessage = s.getAs[Boolean](aNum.toString) match {
        case Some(true)  ⇒ true
        case _  ⇒ false
      }
      if (((new Random) nextInt 10) < 1) State.empty + ("queue", !hasMessage)
      else State.empty + ("queue", hasMessage)
    }
  )

  class simpleAgent extends Agent {
    def policy = { case u: UpdateAgent  ⇒ {

      println("Received reward: "+u.reward)

      if (agentNumber == 1) {
        if (((new Random) nextInt 10) < 1) takeAction('wait)
        else takeAction('send)
      }
      if (agentNumber == 2) {
        if (((new Random) nextInt 10) < 1) takeAction('send)
        else takeAction('wait)
      }
    }}
  }

}