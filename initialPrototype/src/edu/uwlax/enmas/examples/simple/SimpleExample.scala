package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas._, edu.uwlax.enmas.server._
import scala.collection.immutable.HashMap,
  scala.actors.remote._, scala.actors.remote.RemoteActor._

/**
	* Creates a new POMDP by supplying the initial state and the 
	* transition function.
	* Instantiates a SimServer. */
object SimpleServerLauncher extends App {
  new SimServer(
    new POMDP(
      // initial state
      State.empty + ("time" -> 0),

      // transition fxn
      (s: State, aa: Set[AgentCase]) => s.getAs[Int]("time") match {
          case Some(t) => { println(t); s + (("time", t+1)) }
          case _ => s
      }
    ),

    SimpleProxyAgentFactory, // agent proxy builder

    (2, 3) // min, max number of clients
  )
}

/** Instantiates a SimpleAgent */
object SimpleClientLauncher extends App {
  new SimpleAgent("localhost", SimServer.defaultServerPort)
}