import org.enmas.pomdp._, org.enmas.client._, org.enmas.messaging._,
       scala.util._

class simpleAgent extends Agent {
  def name = "Simple Agent"
  def policy(observation: Observation, reward: Float): Action = {
    observation.getAs[Int]("time") map { t  ⇒ if (t % 1000 == 0) {
		  println(t.toString) }
	  }
    if ((new Random nextInt 10) < 7) 'win
    else 'lose
  }
}