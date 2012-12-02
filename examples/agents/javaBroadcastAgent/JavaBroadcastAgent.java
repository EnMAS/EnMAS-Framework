package org.enmas.examples;

import scala.Symbol;
import scala.util.*;
import org.enmas.pomdp.*;
import static org.enmas.pomdp.package$.*;
import org.enmas.client.Agent;
import java.util.NoSuchElementException;

/** BroadcastAgent is very simple!
  * Agent 1: 90% of the time sends and 10% of the time waits.
  * Agent 2: 10% of the time sends and 90% of the time waits.
  */
class JavaBroadcastAgent extends Agent {

	Random random = new Random();

    public String name() { return "Java Broadcast Agent"; }

	public Symbol policy(State observation, float reward) {
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

		Action decision = new Action();
		int rand = random.nextInt(10);
		if (agentNumber() == 1) {
			if (rand < 1) decision = new Action("wait");
			else decision = new Action("send");
		}
		else if (agentNumber() == 2) {	
			if (rand < 1) decision = new Action("send");
			else decision = new Action("wait");
		}
		return decision;
	}
}