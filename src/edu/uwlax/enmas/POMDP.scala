package edu.uwlax.enmas
import scala.collection.immutable._, scala.reflect.{Manifest, ClassManifest}
import ClassManifest.fromClass

class StateException(msg:String) extends Exception(msg)

class State extends ListMap[String, Any] {
	val agentMapping:(String, Set[Agent]) = find(POMDP.agentMappingPredicate) match {
		case Some(tuple:Tuple2[_,_]) => 
			(tuple._1.asInstanceOf[String], tuple._2.asInstanceOf[Set[Agent]])
		case None => ("", ListSet[Agent]())
	}

	if (count(POMDP.agentMappingPredicate) != 1)
		throw new StateException("State must contain exactly one Set[Agent].")

	agentMapping._2.map((a:Agent) =>
		if (agentMapping._2.count((b:Agent) => b.id == a.id) != 1)
			throw new StateException("All agent IDs must be unique."))
}

case class Agent(
	id: Int,
	fO: State => Map[String, Any],				// observation
	fA: State => Set[State => State],			// actions
	fR: State => Float,							// reward
	action: State => State = POMDP.NO_ACTION
)

class POMDP(state: State, substateResolver: Set[State] => State) {

	var history = state::Nil

	def iterate[A<:Agent](agents: List[A]) = {
		history :+= transition(history.last, agents)
		agents.foreach(
			_ match { case ra:RemoteAgent => {
				ra.update(
					ra.fR(history.last), 
					ra.fO(history.last),
					ra.fA(history.last)
				)}})
	}

	def transition(state: State, agents: List[Agent]) = {
		substateResolver(ListSet[State](
			(for (a <- agents) yield a.action(state) 
				+ ((state.agentMapping._1, ListSet(
					for (a <- agents) yield Agent(a.id, a.fO, a.fA, a.fR, a.action))))
			).reduceLeft(_ ++ _).asInstanceOf[State]))
	}
}
object POMDP {
	val NO_ACTION = (s: State) => s // the identity action
	// returns true if the 2nd element of the tuple is a subclass of Set[Agent]

	val agentMappingPredicate = (mapping: (String, Any)) =>
		fromClass(classOf[Set[Agent]]) <:< 
		fromClass(mapping._2.asInstanceOf[AnyRef].getClass())
}
