import org.enmas.server.POMDPIteration,
       org.enmas.client._,
       scala.swing._

class SimpleReporter extends IterationClient {

  def handleIteration(iteration: POMDPIteration) {
    if (iteration.ordinality % 1000 == 0) {
      frame.stateDetails.text = "iteration %s:\n\n%s\n\n".format(
        iteration.ordinality, iteration.state
      ) + iteration.rewards.foldLeft("") { (s, item)  ⇒ {
        s + "Agent [%s] received a reward of [%s]\n".format(
          item._1.agentNumber, item._2
        )
      }}
    }
  }

  val frame = new Frame {
    title = "EnMAS: Simple Reporter"
    contents = stateDetails
    minimumSize = new Dimension(300, 400)
    centerOnScreen
    visible = true

    lazy val stateDetails = new TextArea {
      editable = false; lineWrap = true; wordWrap = true;
    }
  }
}
