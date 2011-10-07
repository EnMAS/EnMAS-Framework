package edu.uwlax.enmas.server

import scala.actors._

/** Simple factory class used by [[edu.uwlax.enmas.server.SimServer]] for 
  * creating new proxies to handle client connections. */
abstract class ProxyAgentFactory {
  /** Returns a new instance of some concrete subclass of ProxyAgent. */
  def build(actor: AbstractActor, name: Symbol): ProxyAgent
}