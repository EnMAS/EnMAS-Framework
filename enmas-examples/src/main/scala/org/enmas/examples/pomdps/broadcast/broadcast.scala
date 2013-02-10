package org.enmas.examples.pomdp.broadcast

import org.enmas.pomdp._
import org.enmas.pomdp.State.Implicits._
import scala.util.Random

case class Broadcast() extends POMDP (
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

  // accomodates exactly 2 agents of type 'RelayAgent
  agentConstraints = List(AgentConstraint('RelayAgent, 2, 2)),

  // state consists of two (Int, Boolean) mappings, representing
  // the internal buffers of agent 1 and agent 2
  initialState = State("1" -> false, "2" -> false),

  actionsFunction = (agentType) => Set(Action("send"), Action("wait")),

  transitionFunction = (state, actions) => {

    val a1HasMessage = state.getAs[Boolean]("1") getOrElse false

    val a2HasMessage = state.getAs[Boolean]("2") getOrElse false

    val a1Sends = actions.exists { a =>
      a.agentNumber == 1 && a.action == Action("send") }

    val a2Sends = actions.exists { a =>
      a.agentNumber == 2 && a.action == Action("send") }

    val defaultDistribution = List(
      (State("1" -> true, "2" -> true), 9),
      (State("1" -> true, "2" -> false), 81),
      (State("1" -> false, "2" -> true), 1),
      (State("1" -> false, "2" -> false), 9)
    )

    (a1Sends, a2Sends, a1HasMessage, a2HasMessage) match {

      // both do legit send OR both wait: stay in the current state
      case (true, true, true, true) | (false, false, true, true) => List(state -> 1)

      case (true, false, _, true) | (false, false, false, true) => List(
        (State("1" -> true, "2" -> true), 9),
        (State("1" -> false, "2" -> true), 1)
      )

      // a1 does legit send, a2 has no msg or waits
      case (true, false, true, _) => List(
        (State("1" -> true, "2" -> true), 1),
        (State("1" -> false, "2" -> true), 9)
      )

      case (false, false, true, false) => List(
        (State("1" -> true, "2" -> true), 1),
        (State("1" -> true, "2" -> false), 9)
      )

      case _ => defaultDistribution
    }
  },

  // 1 if exactly one agent with a message chose to send and 0 otherwise
  rewardFunction = (s, actions, sPrime) => (aNum, agentType) => {

    val sentMessages = actions.filter { a =>
      val aMessage = s.getAs[Boolean](a.agentNumber.toString) getOrElse false
      val choseToSend = a.action == Action("send")
      aMessage && choseToSend
    }

    if (sentMessages.size == 1) 1 else 0
  },

  // lies to the agent 10% of the time
  observationFunction = (s, actions, sPrime) => (aNum, aType) => {
    val hasMessage = s.getAs[Boolean](aNum.toString) getOrElse false
    if ((new Random).nextDouble < 0.1) State("queue" -> ! hasMessage)
    else State("queue" -> hasMessage)
  }
)
