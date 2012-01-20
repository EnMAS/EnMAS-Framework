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

	public void handleError(Throwable error) {}

	public Symbol handleUpdate(State observation, float reward) {
	    System.out.println("I am agent "+agentNumber()+"\nI think my queue is ");
		try {
			Boolean observedMessage = observation.getBoolean("queue"));
			if (observedMessage) System.out.println("full");
			else System.out.println("empty");
		}
		catch (NoSuchElementException nse) {
			System.out.println("(oops: error observing queue)");
		}
	    System.out.println("I received "+reward+" as a reward\n");

		Symbol decision = NO_ACTION();
		int rand = (new Random()).nextInt(10);
		if (agentNumber() == 1) {
			if (rand < 1) decision = new Symbol("wait");
			else decision = new Symbol("send");
		}
		else if (agentNumber() == 2) {	
			if (rand < 1) decision = new Symbol("send");
			else decision = new Symbol("wait");
		}
		return decision;
	}
}