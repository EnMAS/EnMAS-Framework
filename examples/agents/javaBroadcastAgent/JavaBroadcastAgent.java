import org.enmas.client.Agent;
import org.enmas.pomdp.State;
import org.enmas.pomdp.Action;
import java.util.NoSuchElementException;
import java.util.Random;

/** BroadcastAgent is very simple!
  * Agent 1: 90% of the time sends and 10% of the time waits.
  * Agent 2: 10% of the time sends and 90% of the time waits.
  */
class JavaBroadcastAgent extends Agent {

	final Action waitAction = new Action("wait");
	final Action sendAction = new Action("send");
	Random random = new Random();
	final double randomFactor = 0.1;

    public String name() { return "Java Broadcast Agent"; }

	public Action policy(State observation, float reward) {
	    System.out.println("I am agent "+agentNumber()+"\nI think my queue is ");
		try {
			Boolean observedMessage = observation.getBoolean("queue");
			if (observedMessage) System.out.println("full");
			else System.out.println("empty");
		}
		catch (NoSuchElementException nse) {
			System.out.println("(oops: error observing queue)");
		}
	    System.out.println("I received "+reward+" as a reward\n");

		Action decision = Action.DoNothing();
		if (agentNumber() == 1) {
			decision = (random.nextDouble() < randomFactor)
				? waitAction
				: sendAction;
		}
		else { // agentNumber() == 2
			decision = (random.nextDouble() < randomFactor)
				? sendAction
				: waitAction;
		}

		return decision;
	}
}