import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

/** BroadcastAgent is very simple!
  * Agent 1: 90% of the time sends and 10% of the time waits.
  * Agent 2: 10% of the time sends and 90% of the time waits.
  */
class BroadcastAgent extends Agent {

  def name = "Simple Broadcast Agent"

  def policy(observation: Observation, reward: Float): Action = {
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
