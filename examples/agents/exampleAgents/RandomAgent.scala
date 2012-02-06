import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

class RandomAgent extends Agent {
  def name = "A. Random Agent, Esq."
  val random = new scala.util.Random
  def policy(observation: Observation, reward: Float) = {
    actions.toSeq(random nextInt actions.size)
  }
}
