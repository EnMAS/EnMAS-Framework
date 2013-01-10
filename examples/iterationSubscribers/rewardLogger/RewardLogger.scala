import org.enmas.server.POMDPIteration,
       org.enmas.client._

import java.io.{FileOutputStream, OutputStreamWriter}

class RewardLogger extends IterationClient {

  val out = new OutputStreamWriter(new FileOutputStream(
    "rewardLogger_%d.txt" format System.currentTimeMillis
  ))
  out.write("iteration, agent, reward\n")

  def handleIteration(iteration: POMDPIteration) {
    iteration.rewards map { item => {
      out.write(
        "%s, %s, %s\n".format(
          iteration.ordinality, item._1.agentNumber, item._2
        )
      )
    }}
  }
}