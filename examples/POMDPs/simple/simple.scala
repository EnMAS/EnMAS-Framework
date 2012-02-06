import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

object Simple {
  val simpleModel = POMDP (
    name = "Example POMDP Model",
    description = "Just for illustration",
    agentConstraints = List ( AgentConstraint('A1, 1, 1), AgentConstraint('A2, 1, 1)),
    initialState = State("time"  → 0),
    actionsFunction = (_)  ⇒ Set('win, 'lose),
    transitionFunction = (state, _)  ⇒ {
      state.getAs[Int]("time") match {
        case Some(t)  ⇒ List((State("time"  → (t+1)), 1))
        case _  ⇒ List((state, 1))
      }
    },
    rewardFunction = (state, actions, _)  ⇒ (_)  ⇒ {
      if (actions.foldLeft(true)( (a, b)  ⇒ { a && b.action == 'win })) 1
      else 0
    },
    observationFunction = (state, _, _)  ⇒ (_, _)  ⇒ state
  )
}
