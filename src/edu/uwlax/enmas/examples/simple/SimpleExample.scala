package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas._, edu.uwlax.enmas.server._
import scala.collection.immutable.HashMap,
  scala.actors.remote._, scala.actors.remote.RemoteActor._

/**
	* Creates a new POMDP by supplying the initial state and the arbiter function.
	* Instantiates a SimpleAgentProxy (subclass of AgentProxy).
	* Instantiates a SimServer. */
object SimpleServerLauncher extends App {
  val server = new SimServer(
    new POMDP(HashMap.empty, (ss: Set[State]) => ss.head),
    SimpleAgentProxyFactory,
    9700, // port
    'TestServer // app server name on node
  )
}

/** Instantiates a SimpleAgent */
object SimpleClientLauncher extends App {
  val client = new SimpleAgent(
    select(
      Node("localhost", 9700), 'TestServer
    )
  )
}