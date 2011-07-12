package edu.uwlax.enmas

/* proxy class for client AI agent */

abstract class RemoteAgent(
	id: Int,
	fO: State => Map[String, Any],				// observation
	fA: State => Set[State => State],			// actions
	fR: State => Float,							// reward
	action: State => State = POMDP.NO_ACTION
) extends Agent(id, fO, fA, fR, action) {

	def update(
		reward: Float,
		observation: Map[String, Any],
		actions: Set[State => State]
	);

	override def action(): State => State;

}
