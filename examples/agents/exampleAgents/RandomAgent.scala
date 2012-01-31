import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._

class RandomAgent extends Agent {
  val random = new scala.util.Random
  def name = "A. Random Agent, Esq."
  def handleError(error: Throwable) {}
  def handleUpdate(observation: Observation, reward: Float) = {
    actions.toSeq(random nextInt actions.size)
  }
}
