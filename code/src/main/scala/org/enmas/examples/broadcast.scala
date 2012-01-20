package org.enmas.examples

import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

/** There are two agents in this scenario.  Each agent represents
  * a relay on a network.  The agents receive messages, which are stored
  * in a local buffer which holds at most one message at a time.  At each
  * time step, each agent can choose to "send" (forward the message) 
  * or to "wait".  Both agents are rewarded whenever a message is sent
  * successfully.  However!  The agents share a single outbound serial line
  * and if BOTH agents a) have a message and b) choose to "send", a
  * collision results and both send attempts fail.  At each time step,
  * agents are updated with a reward from the previous step (either 1 or 0)
  * and an observation (a Boolean value) indicating whether their sensors
  * indicate that a message is waiting in their local buffer.  This value
  * is noisy.  10% of the time, the observed sensor value indicates the
  * wrong state of the buffer.
  */
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

    // accomodates exactly 2 agents of type 'A1
    agentConstraints = List(AgentConstraint('A1, 2, 2)),

    // state consists of two (Int, Boolean) mappings, representing
    // the internal buffers of agent 1 and agent 2
    initialState = State.empty + ("1"  → false) + ("2"  → false),

    // each agent has the same set of actions
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

    // 1 if exactly one agent with a message chooses 'send and 0 otherwise
    rewardFunction = (s, actions, sPrime)  ⇒ (agentType)  ⇒ {
      actions.filter{ a  ⇒ { a.action == 'send &&
        s.getAs[Boolean](a.agentNumber.toString).getOrElse(false)
      }}.size match {
        case 1  ⇒ 1
        case _  ⇒ 0
      }
    },

    // lies to the agent 10% of the time
    observationFunction = (s, actions, sPrime)  ⇒ (aNum, aType)  ⇒ {
      val hasMessage = s.getAs[Boolean](aNum.toString) getOrElse false
      if ((new Random nextInt 10) < 1) State.empty + ("queue", !hasMessage)
      else State.empty + ("queue", hasMessage)
    }
  )

  /** BroadcastAgent is very simple!
    * Agent 1: 90% of the time sends and 10% of the time waits.
    * Agent 2: 10% of the time sends and 90% of the time waits.
    */
  class BroadcastAgent extends Agent {
    
    def handleError(error: Throwable) {}
    
    def handleUpdate(observation: Observation, reward: Float): Action = {
      print("I am agent "+agentNumber+"\nI think my queue is ")
      println(observation.getAs[Boolean]("queue").getOrElse(false) match {
        case true  ⇒ "full"
        case _  ⇒ "empty"
      })
      println("I received "+reward+" as a reward\n")
      var decision: Action = NO_ACTION
      if (agentNumber == 1)
        if ((new Random nextInt 10) < 1) decision = 'wait else decision = 'send

      if (agentNumber == 2)
        if ((new Random nextInt 10) < 1) decision = 'send else decision = 'wait
        
      decision
    }
  }

}