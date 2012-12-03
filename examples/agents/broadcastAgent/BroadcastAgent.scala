import org.enmas.client.Agent
import org.enmas.pomdp.{State, Action}
import scala.util.Random

/** BroadcastAgent is very simple!
  * Agent 1: 90% of the time sends and 10% of the time waits.
  * Agent 2: 10% of the time sends and 90% of the time waits.
  */
class BroadcastAgent extends Agent {

  def name = "Simple Broadcast Agent"

  val waitAction = Action("wait")
  val sendAction = Action("send")
  val random = new Random
  val randomFactor = 0.1

  def policy(observation: State, reward: Float): Action = {
    
    val queueState =
      if (observation.getAs[Boolean]("queue").getOrElse(false)) "full"
      else "empty"

    println(
      (
        "I am agent %d\nI think my queue is %s\n" +
        "My reward from last round is %f\n"
      ).format(agentNumber, queueState, reward)
    )

    if (agentNumber == 1)
      if (random.nextDouble < randomFactor) waitAction
      else sendAction
    else // agentNumber == 2
      if (random.nextDouble < randomFactor) sendAction
      else waitAction
  }
}
