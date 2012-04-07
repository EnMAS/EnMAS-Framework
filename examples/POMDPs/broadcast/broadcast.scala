import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

case class Broadcast extends POMDP (
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
  initialState = State.empty + ("1"  → false) + ("2"  → false),

  // each agent has the same set of actions
  actionsFunction = (agentType)  ⇒ Set('send, 'wait),

  transitionFunction = (state, actions)  ⇒ {
    val a1HasMessage = state.getAs[Boolean]("1") getOrElse false
    val a2HasMessage = state.getAs[Boolean]("2") getOrElse false
    val a1Sends = actions.filter { a: AgentAction  ⇒
      a.agentNumber == 1 && a.action == 'send }.length > 0
    val a2Sends = actions.filter { a: AgentAction  ⇒
      a.agentNumber == 2 && a.action == 'send }.length > 0

    val defaultDistribution = List(
      (State("1"  → true) + ("2"  → true), 9),
      (State("1"  → true) + ("2"  → false), 81),
      (State("1"  → false) + ("2"  → true), 1),
      (State("1"  → false) + ("2"  → false), 9)
    )

    (a1Sends, a2Sends, a1HasMessage, a2HasMessage) match {
      // both do legit send OR both wait: stay in the current state
      case (true, true, true, true) | (false, false, true, true)  ⇒ List(state  → 1)
      case (true, false, _, true) | (false, false, false, true)  ⇒ { List(
        (State("1"  → true) + ("2"  → true), 9),
        (State("1"  → false) + ("2"  → true), 1)
      )}
      // a1 does legit send, a2 has no msg or waits
      case (true, false, true, _)  ⇒ { List(
        (State("1"  → true) + ("2"  → true), 1),
        (State("1"  → false) + ("2"  → true), 9)
      )}
      case (false, false, true, false)  ⇒ { List(
        (State("1"  → true) + ("2"  → true), 1),
        (State("1"  → true) + ("2"  → false), 9)
      )}
      case _  ⇒ defaultDistribution
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
    if ((new Random nextInt 10) < 1) State("queue"  → ! hasMessage)
    else State("queue"  → hasMessage)
  }
)
