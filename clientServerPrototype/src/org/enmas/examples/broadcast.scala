package org.enmas.examples

import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

object Broadcast {
  val broadcastProblem = POMDP (
    name = "Broadcast Problem",
    description = """
There are two agents in this scenario.  Each agent represents
a relay on a network.  The agents receive messages, which are stored
in a local buffer which holds at most one message at a time.  At each
time step, each agent can choose to "send" (forward the message) 
or to "wait".  Both agents are rewarded whenever a message is sent
successfully.  However!  The agents share a single outbound serial line
and if BOTH agents a) have a message and b) choose to "send", a
collision results and both send attempts fail.  At each time step,
agents are updated with a reward from the previous step (either 1 or 0)
and an observation (a Boolean value) indicating whether their sensors
indicate that a message is waiting in their local buffer.  This value
is noisy.  10% of the time, the observed sensor value indicates the
wrong state of the buffer.
""",

    agentConstraints = List(AgentConstraint('A1, 2, 2)),

    initialState = State.empty + ("1"  → false) + ("2"  → false),

    actionsFunction = (agentType)  ⇒ Set('send, 'wait),

    transitionFunction = (state, actions)  ⇒ {
      val m1 = state.getAs[Boolean]("1") getOrElse false
      val m2 = state.getAs[Boolean]("2") getOrElse false
      val a1 = actions contains {
        a: AgentAction  ⇒ a.agentNumber == 1 && a.action == 'send }
      val a2 = actions contains {
        a: AgentAction  ⇒ a.agentNumber == 2 && a.action == 'send }

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

    rewardFunction = (s, actions, sPrime)  ⇒ (agentType)  ⇒ {
      val sendActions = actions filter {_.action == 'send}
      sendActions.size match {
        case 1  ⇒ {
          val a = sendActions.head
          s.getAs[Boolean](a.agentNumber.toString) match {
            case Some(true)  ⇒ 1
            case _  ⇒ 0
          }
        }
        case 2  ⇒ {
          val test = actions filter { a: AgentAction  ⇒ {
            s.getAs[Boolean](a.agentNumber.toString) getOrElse false
          }}
          if (test.size == 1) 1 else 0
        }
        case _  ⇒ 0
      }
    },

    observationFunction = (s, actions, sPrime)  ⇒ (aNum, aType)  ⇒ {
      val hasMessage = s.getAs[Boolean](aNum.toString) getOrElse false
      if (((new Random) nextInt 10) < 1) State.empty + ("queue", !hasMessage)
      else State.empty + ("queue", hasMessage)
    }
  )

  /** simpleAgent lives up to its name!
    *
    * 90% of the time it sends and 10% of the time it waits
    * without regard for observations or rewards
    */
  class simpleAgent extends Agent {
    def policy = { case u: UpdateAgent  ⇒ {
      print("I think my queue is ")
      println(u.observation.getAs[Boolean]("queue").getOrElse(false) match {
        case true  ⇒ "full"
        case _  ⇒ "empty"
      })
      println("I received "+u.reward+" as a reward\n")
      if (agentNumber == 1)
        if (((new Random) nextInt 10) < 1) takeAction('wait)
        else takeAction('send)

      if (agentNumber == 2)
        if (((new Random) nextInt 10) < 1) takeAction('send)
        else takeAction('wait)
    }}
  }
}