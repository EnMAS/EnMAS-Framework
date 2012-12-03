import org.enmas.pomdp._
import org.enmas.pomdp.State.Implicits._

case class CliffWorld extends POMDP (

  name = "Cliff Problem",

  description = """Two agents wander around a small grid.  There is a
goal position where the agents receive a large reward.  At the bottom
of the grid is a cliff.  If the agents walk off the cliff they receive
a large penalty.""",

  agentConstraints = List(
    AgentConstraint('Q, 1, 1),
    AgentConstraint('S, 1, 1)
  ),

  initialState = State("Q" -> (0, 0), "S" -> (0, 0)),

  actionsFunction = (_) => Set(
    Action("north"),
    Action("south"),
    Action("east"),
    Action("west")
  ),

  transitionFunction = (state, actions) => {
  	val qPos = state.getAs[(Int, Int)]("Q") getOrElse (0, 0)
  	val sPos = state.getAs[(Int, Int)]("S") getOrElse (0, 0)

  	val qAction: Action = actions find { _.agentType == 'Q } match {
  		case Some(agentAction) => agentAction.action
  		case None => Action("north")
  	}

  	val sAction: Action = actions find { _.agentType == 'S } match {
  		case Some(AgentAction(_, _, action)) => action
  		case None => Action("north")
  	}

  	def move(pos: (Int, Int), direction: Action): (Int, Int) = {
  		val (x, y) = pos
  		direction.name match {
  			case "north" => {
  				if (x >= 3) pos
  				else (x + 1, y)
  			}
  			case "south" => {
  				if (x <= 1) (0, 0)
  				else (x - 1, y)
  			}
  			case "east" => {
  				if (y >= 11 || pos == (0, 0)) pos
  				else (x, y + 1)
  			}
  			case "west" => {
  				if (y <= 0) pos
  				else (x, y - 1)
  			}
        case _ => pos
  		}
  	}

  	State(
      "Q" -> move(qPos, qAction),
      "S" -> move(sPos, sAction)
    )
  },

  rewardFunction = (state, actions, _) => (agentType) => {

  	val agentPos = state.getAs[(Int, Int)](agentType.name) getOrElse (0, 0)

  	val agentAction = actions.collectFirst {
      case AgentAction(_, agentType, action) => action
    } getOrElse Action("north")

  	val (x, y) = agentPos

  	val agentFell = x == 1 && y <= 10 && y > 0 &&
                    agentAction == Action("south")

  	val agentWon = x == 1 && y == 11 &&
                   agentAction == Action("south")

  	if (agentFell) -100000
  	else if (agentWon) 100
  	else -1 // any other case
  },

  observationFunction = (state, actions, statePrime) => (aNum, aType) =>
    if (aType == 'Q) state - "S" else state - "Q"
)