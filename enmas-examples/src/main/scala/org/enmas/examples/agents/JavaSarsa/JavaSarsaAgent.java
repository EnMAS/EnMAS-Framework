package org.enmas.examples.agents.javaSarsa;

import org.enmas.pomdp.Agent;
import org.enmas.pomdp.Action;
import org.enmas.pomdp.State;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.AbstractMap.SimpleImmutableEntry; // poor man's 2-tuple

/** 
* Implementes the SARSA reinforcement learning algorithm, a modified
* version of Q-Learning.
*/
public class JavaSarsaAgent extends Agent {

	/** Represents a (State, Action) tuple. */
	private class Situation extends SimpleImmutableEntry<State, Action> {
		public Situation(State state, Action action) { super(state, action); }
	}

	final Random random = new Random();
	final double randomFactor = 0.1; // this agent acts randomly 10% of the time
	final float alpha = 0.9f;
	final float gamma = 0.9f;
	HashMap<Situation, Float> qTable = new HashMap<Situation, Float>();
	Situation lastSA = new Situation(State.empty(), Action.DoNothing());

	public String name() {
		return "Java Sarsa Agent";
	}

	public Action policy(State observation, float reward) {

		Map<Float, Action> options = new HashMap<Float, Action>();
		scala.collection.Iterator<Action> it = actions().iterator();
		while (it.hasNext()) {
			Action action = it.next();
			options.put(expectedReward(observation, action), action);
		}

		Action bestGuess = options.get(
			new TreeSet<Float>(options.keySet()).last()
		);

		Action decision = bestGuess;

		if (random.nextDouble() < randomFactor) { decision = randomAction(); }

		Situation currentSA = new Situation(observation, decision);

		float currentExpected = expectedReward(observation, decision);

		float lastExpected = expectedReward(
			lastSA.getKey(),
			lastSA.getValue()
		);

		float newQValue = lastExpected + alpha * (
			reward + gamma * currentExpected - lastExpected
		);

		qTable.put(lastSA, newQValue); // update the Q-Table
		lastSA = currentSA;

		return decision;
	}

	float expectedReward(State state, Action action) {
		Float expected = qTable.get(new Situation(state, action));
		return (expected != null) ? expected : 0;
	}

	Action randomAction() {
		return actions().toSeq().apply(random.nextInt(actions().size()));
	}
}