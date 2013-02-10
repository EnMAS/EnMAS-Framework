package org.enmas.pomdp

/**
  * Represents an action that may be taken by an Agent.
  */
sealed case class Action(name: String = "") {
  override def toString() = name
}

object Action {
  val DoNothing = Action()
}