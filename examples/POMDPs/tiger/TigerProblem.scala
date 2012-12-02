import org.enmas.pomdp._, scala.util._

case class TigerProblem extends POMDP (

  name = "One Door Tiger Problem",

  description = """In this problem, there are two agents, one tiger, and a
single door between them.  The tiger lurks near the door, but occasionally
wanders away for a while.  The agents listen at the door for growls and
other tiger-like noises.  On the tiger's side of the door, there is a
baked bean that the agents can share.  If either agent opens the door
while the tiger is not there they both get half a baked bean.  However, if
the tiger is there they are both mauled by the tiger.""",

  agentConstraints = List(
    AgentConstraint('TweedleDee, 1, 1),
    AgentConstraint('TweedleDum, 1, 1)
  ),

  initialState = State("time" → 0) +
    ("tiger"  → true) +
    ("doorOpen"  → false) +
    ("tigerNoise"  → true),

  actionsFunction = (_)  ⇒ Set(Action("open"), Action("listen")),

  transitionFunction = (state, actions)  ⇒ {
    val random = new Random
    val time = state.getAs[Int]("time") getOrElse 0
    val tiger = state.getAs[Boolean]("tiger") getOrElse false
    val doorOpen = state.getAs[Boolean]("doorOpen") getOrElse false
    val tigerNoise = state.getAs[Boolean]("tigerNoise") getOrElse false

    val tigerPrime = (random nextInt 10) < (if (tiger) 8 else 3)

    State("time"  → (time + 1)) +
      ("tiger"  → tigerPrime) +
      ("doorOpen"  → (actions.filter { _.action == Action("open") }.length > 0)) +
      ("tigerNoise"  → (if (tigerPrime) (random nextInt 10) < 7 else false))
  },

  rewardFunction = (state, actions, _)  ⇒ (_)  ⇒ {
    val tiger = state.getAs[Boolean]("tiger") getOrElse false
    val doorOpen = state.getAs[Boolean]("doorOpen") getOrElse false

    // reward values for each possible outcome
    val survival = 1
    val treat = 10
    val death = -50

    if (tiger && doorOpen) death
    else if (! tiger && doorOpen) treat
    else survival
  },

  observationFunction = (state, _, _)  ⇒ (_, _)  ⇒ {
    val random = new Random
    val tigerNoise = state.getAs[Boolean]("tigerNoise") getOrElse false
    val doorOpen = state.getAs[Boolean]("doorOpen") getOrElse false

    State("doorOpen"  → doorOpen) +
      ("tigerNoise"  → (
        if ((random nextInt 10) < 9) tigerNoise else ! tigerNoise))
  }
)