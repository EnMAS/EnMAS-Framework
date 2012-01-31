import org.enmas.server.POMDPIteration,
       org.enmas.client._

class RewardReporter extends IterationClient {
  def handleIteration(iteration: POMDPIteration) {
    println("\nRewards for iteration #[%s]:" format iteration.ordinality)
    iteration.rewards map { item  ⇒ {
      println("Agent [%s] received a reward of [%s]".format(item._1.agentNumber, item._2))
    }}
  }
}
