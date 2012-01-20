package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.server.ServerSpec, org.enmas.client.gui._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._, akka.dispatch._,
       akka.util.Timeout, akka.util.duration._

class Session(server: ActorRef) extends Actor {
  import ClientManager._, context._

  private var uniqueID = -1
  private var agents = Map[Int, (AgentType, ActorRef)]()
  private val gui = new SessionGUI(self)

  /** Returns true iff registering this clientManager session
    * with the specified server succeeds.
    *
    * Reasons that lead to registration failure are varied:
    * server is unreachable, server is not running, request denied,
    * or some other exceptional condition.
    */
  private def registerHost(): Boolean = {
    var result = false
    try { Await.result(server ? RegisterHost(self), timeout.duration) match {
      case c: ConfirmHostRegistration  ⇒ {
        uniqueID = c.id
        watch(server) // subscribe to Terminated('server)
        result = true
      }
      case _  ⇒ println("Registration denied by server!")
    }}
    catch { case t: Throwable  ⇒ {
      println("An error occurred during registration:")
      t.printStackTrace
    }}
    result
  }

  /** Returns true iff the server confirms the agent registration
    * and creating the agent succeeds.
    *
    * Upon successful registration, the agent is started and forwarded
    * the ConfirmAgentRegistration from the server.  The agent uses that
    * information for initialization and then become()s its user-defined
    * policy function.
    */
  private def registerAgent(
    agentType: AgentType, 
    clazz: java.lang.Class[_ <: Agent]
  ): Boolean = {
    var result = false
    try { Await.result(
      server ? RegisterAgent(uniqueID, agentType), timeout.duration
    ) match {
      case confirmation: ConfirmAgentRegistration  ⇒ {
        val client = actorOf(Props(clazz.newInstance repliesTo self))
        agents += (confirmation.agentNumber  → (agentType, client))
        client forward confirmation
        result = true
      }
      case _  ⇒ ()
    }}
    catch { case _  ⇒ () }
    result
  }

  def receive = {
    case 'Init  ⇒ sender ! registerHost

    case m: LaunchAgent  ⇒ sender ! registerAgent(m.agentType, m.clazz)

    case MessageBundle(content)  ⇒ {
      if (sender == server) content map {
        c  ⇒ agents.find(_._1 == c.agentNumber) map { a  ⇒ a._2._2 ! c }}
    }

    case t: TakeAction  ⇒ {
      if (! (agents contains t.agentNumber)) sender ! PoisonPill
      agents.get(t.agentNumber) map { tuple  ⇒
        if (sender == tuple._2) server forward t else sender ! PoisonPill }
    }

    case Terminated(deceasedActor)  ⇒ {
      if (deceasedActor == server) {
        println("The server died! Shutting down the session...")
        agents map { a  ⇒ { unwatch(a._2._2); stop(a._2._2) }}
        stop(self)
      }
      else { agents.find(_._2._2 == deceasedActor) match { case Some(deadAgent)  ⇒ {
          println("One of my agents died! It's an awful shame...")
          server ! AgentDied(deadAgent._1)
          agents = agents filterNot { _ == deadAgent }
        }
        case None  ⇒ println("Something died but I couldn't figure out what it was!")
      }}
    }

    case AgentDied(id)  ⇒ {
      println("An agent on another host (number "+id+") has died.")
    }
 
    case _  ⇒ () // ignore unhandled messages

  }
}