package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas.server._, edu.uwlax.enmas.server.Mode._
import scala.actors._

object SimpleAgentProxyFactory extends AgentProxyFactory {
  // the server delegates to this function to create new proxies
  def build(actor: AbstractActor, name: Symbol): AgentProxy = 
    new SimpleAgentProxy(actor, name, ASYNCHRONOUS)
}