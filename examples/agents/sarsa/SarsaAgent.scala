import org.enmas.pomdp._, org.enmas.client._,
       scala.util._

/**
  * Implementes the SARSA reinforcement learning algorithm, a modified
  * version of Q-Learning.
  */
class SarsaAgent extends Agent {

  def name = "Sarsa Agent"
  val random = new scala.util.Random
  val alpha = 0.9f
  val gamma = 0.9f
  var qTable = Map[(State, Action), Float]()
  var lastSA = (State(), NO_ACTION)

  def policy(observation: State, reward: Float): Action = {

    lazy val options: List[(Action, Float)] = {
      for (a <- actions) yield (a, expectedReward(observation, a))
    }.toList

    lazy val best = options.sortWith {
      (op1, op2) => op1._2 > op2._2
    }.head._1

    var decision = if ((random nextInt 10) < 1)
                     actions.toSeq(random nextInt actions.size)
                   else
                     best

    val newQValue = expectedReward(lastSA._1, lastSA._2) +
                    alpha * (
                      reward +
                      gamma * expectedReward(observation, decision) -
                      expectedReward(lastSA._1, lastSA._2)
                    )

    qTable = qTable + (lastSA -> newQValue) 
    lastSA = (observation, decision)

    decision
  }

  private def expectedReward(state: State, action: Action): Float = 
    qTable.get(state, action) getOrElse 0

}