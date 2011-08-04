package edu.uwlax {


  /** Provides classes for the EnMAS POMDP framework. 
    * The main class in this package is [[edu.uwlax.enmas.POMDP]]. */
  package object enmas {

    type HashSet[A] = scala.collection.immutable.HashSet[A]

    /** Defines a POMDP State to be a set of (String, Any) tuples.
      * The contract for this type is to add only objects that
      * can be properly serialized.  Subsets the State need to be sent
      * over the network to distributed agents. */
    type State = HashSet[(String, Any)]

    /** Defines a POMDP Action to be a function that takes a State
      * as its single argument, returning a new state. */
    type Action = State => State
  }

}
