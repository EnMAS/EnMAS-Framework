package edu.uwlax {

  /** Provides classes for the EnMAS POMDP framework.  EnMAS stands for
    * "Environment for Multi Agent Simulation".
    *
    * == About the Project ==
    * EnMAS is an Environment for Multi-Agent and
    * Team-Based Artificial Intelligence Research.  The project is guided 
    * by current research in MAS, particularly the DEC-POMDP model.
    *
    * The name is pronounced like "en masse".
    *
    * ===== The main goals of the project are as follows: =====
    *
    * 1. Rapid prototyping of POMDP problems for research and teaching purposes
    *
    * 2. Orthogonality between theoretical models and implementation
    *
    * 3. Genericity: abstaining from assumptions about the problem domain
    *
    * == Acknowledgements ==
    *
    * This project was created and is maintained by Connor Doyle 
    * <[[mailto:connor.p.d@gmail.com connor.p.d@gmail.com]]> as part
    * of the Master of Software Engineering degree at the University of
    * Wisconsin - La Crosse under the advisement of 
    * Drs. [[http://cs.uwlax.edu/~mallen Marty Allen]] and
    * [[http://charity.cs.uwlax.edu Kenny Hunt]].
    *
    * The author would like to acknowledge the generosity of the Department
    * of Computer Science at the University of Wisconsin - La Crosse
    * and the National Science Foundation.
    *
    * == Usage Summary ==
    * The main class in this package is [[edu.uwlax.enmas.POMDP]]. 
    *
    * ==== To create a new simulation: ====
    *
    * 1. Implement at least one subclass of [[edu.uwlax.enmas.server.AgentProxy]]
    *
    * 2. Implement a subclass of [[edu.uwlax.enmas.server.AgentProxyFactory]]
    *
    * 3. Create a new [[edu.uwlax.enmas.POMDP]] by supplying the initial state and
    *    the transition function.
    *
    * 4. Create a new [[edu.uwlax.enmas.server.SimServer]], supplying your [[edu.uwlax.enmas.POMDP]] and
    *    an instance of your [[edu.uwlax.enmas.server.AgentProxyFactory]] subclass.  
    *    The server uses the [[edu.uwlax.enmas.server.AgentProxyFactory]] instance
    *    to create proxies on demand as client agents
    *    connect.
    *
    * ==== To create a new agent: ====
    *
    *	1. Implement and instantiate a subclass of AgentClient
    *
    * == Further documentation: ==
    * - The rest of this Scaladoc API Specification
    *
    * - [[http://mse.oneorangesoftware.com The Project Wiki]] 
    *
    * == Legal ==
    * <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/" target="_blank">
    *   <img alt="Creative Commons License" 
    *     style="border-width:0" 
    *     src="http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png" />
    * </a><br />
    * <span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">
    * EnMAS (Environment for Multi-Agent Simulation)</span>
    * by <span xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">Connor Doyle</span>
    * is licensed under a 
    * <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/" target="_blank">
    * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>. */
  package object enmas {

    /** HashMap is preferable to ListMap here because it offers
      * effective constant time performance for add and remove.
      *
      * see:
      * [[http://www.scala-lang.org/docu/files/collections-api/collections_40.html Collections Performance Characteristics]]
      *
      * also see:
      * [[http://akka.io/docs/akka/1.1.3/scala/stm.html#persistent-datastructures Explanation of Persistent Datastructures]] */
    type HashMap[A, B] = scala.collection.immutable.HashMap[A, B]

    /** POMDP Actions are just Symbols */
    type Action = Symbol
  }

}
