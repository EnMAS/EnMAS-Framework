package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._

/** Represents a reference to a remote Agent from the point
  * of view of a Server.
  */
case class AgentSpec(
  sessionID: Int,
  agentNumber: Int,
  agentType: AgentType
)