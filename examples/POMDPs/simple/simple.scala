import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

case class SimplePOMDP extends POMDP (

  name = "Example POMDP",

  description = "Just a simple POMDP",

  agentConstraints = List(
    AgentConstraint('A1, 1, 2),
    AgentConstraint('A2, 1, 2)
  ),

  initialState = State("time"  → 0),

  actionsFunction = (_)  ⇒ Set('win, 'lose),

  transitionFunction = (state, _)  ⇒ {
    val t = state.getAs[Int]("time") getOrElse 0
    State("time"  → (t+1))
  },

  rewardFunction = (state, actions, _)  ⇒ (_)  ⇒ {
    if (actions.foldLeft(true)( (a, b)  ⇒ { a && b.action == 'win })) 1
    else 0
  },

  observationFunction = (state, _, _)  ⇒ (_, _)  ⇒ state
)
