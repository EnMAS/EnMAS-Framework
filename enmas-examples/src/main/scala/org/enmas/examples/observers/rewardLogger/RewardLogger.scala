package org.enmas.examples.observers.rewardLogger

import org.enmas.pomdp.{Observer, POMDPIteration}
import java.io.{FileOutputStream, OutputStreamWriter}

class RewardLogger extends Observer {

  val out = new OutputStreamWriter(new FileOutputStream(
    "rewardLogger_%d.txt" format System.currentTimeMillis
  ))
  out.write("iteration, agent, reward\n")

  def observe(iteration: POMDPIteration) {
    iteration.rewards map { item => {
      out.write(
        "%s, %s, %s\n".format(
          iteration.ordinality, item._1.agentNumber, item._2
        )
      )
    }}
  }
}