package edu.uwlax.enmas.examples.simple

import edu.uwlax.enmas.server._, edu.uwlax.enmas.server.Mode._
import scala.actors._

object SimpleProxyAgentFactory extends ProxyAgentFactory {
  // the server delegates to this function to create new proxies
  def build(actor: AbstractActor, name: Symbol): ProxyAgent = 
    new SimpleProxyAgent(actor, name, ASYNCHRONOUS)
}