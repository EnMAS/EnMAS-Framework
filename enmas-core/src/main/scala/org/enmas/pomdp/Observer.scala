package org.enmas.pomdp

import akka.actor.Actor

/**
  * ...
  */
abstract class Observer extends Actor {

  /**
    * ...
    */
  def observe(iteration: POMDPIteration)

  /**
    * Defers to `this.observe`.
    */
  final def receive = { case i: POMDPIteration => observe(i) }

}
