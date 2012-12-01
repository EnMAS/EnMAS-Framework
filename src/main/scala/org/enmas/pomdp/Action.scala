package org.enmas.pomdp

/**
  * Represents an action that may be taken by an Agent.
  */
case class Action(name: String) {
  override def toString() = name
}
