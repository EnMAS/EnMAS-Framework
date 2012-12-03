import org.enmas.client.Agent
import org.enmas.pomdp.{State, Action}
import scala.util.Random

class RandomAgent extends Agent {

  val random = new scala.util.Random

  def name = "Random Agent"

  def policy(observation: State, reward: Float) =
    actions.toSeq(random nextInt actions.size)
}