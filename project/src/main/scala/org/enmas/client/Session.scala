package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.client.gui._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._, akka.dispatch._, akka.pattern.ask,
       akka.util.Timeout, akka.util.duration._

class Session(server: ServerSpec) extends Actor {
  import ClientManager._, Session._, context._

  watch(server.ref) // subscribe to Terminated(server.ref)

  private var uniqueID = -1
  private var agents = Map[Int, (AgentType, ActorRef)]()
  private var clients = Map[Int, ActorRef]()

  // TODO: fill these sets
//  private var graphicsClients = Map[Int, GraphicsClient]
//  private var graphicsClients = Map[Int, ]

  private val gui = new SessionGUI(self, server)

  /** Returns true iff registering this clientManager session
    * with the specified server succeeds.
    *
    * Reasons that lead to registration failure are varied:
    * server is unreachable, server is not running, request denied,
    * or some other exceptional condition.
    */
  private def registerHost(): Boolean = {
    var result = false
    try { Await.result(server.ref ? RegisterHost(self), timeout.duration) match {
      case c: ConfirmHostRegistration  ⇒ {
        uniqueID = c.id
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

  /** Replies to the sender with a ConfirmAgentRegistration message
    * iff the server confirms the agent registration
    * and creating the agent succeeds, or false otherwise.
    *
    * Upon successful registration, the agent is started and forwarded
    * the ConfirmAgentRegistration from the server.  The agent uses that
    * information for initialization and then become()s its user-defined
    * policy function.
    */
  private def registerAgent(
    agentType: AgentType, 
    clazz: java.lang.Class[_ <: Agent]
  ) {
    try { Await.result(
      server.ref ? RegisterAgent(uniqueID, agentType), timeout.duration
    ) match {
      case confirmation: ConfirmAgentRegistration  ⇒ {
        val agent = actorOf(Props(clazz.newInstance repliesTo self))
        watch(agent)
        agents += (confirmation.agentNumber  → (agentType, agent))
        agent forward confirmation
        sender ! confirmation
      }
      case _  ⇒ sender ! false
    }}
    catch { case _  ⇒ sender ! false }
  }

  private def registerClient(
    clazz: java.lang.Class[_ <: IterationClient]
  ) {
    def nextClientId = clients.foldLeft(0){ _ max _._1 } + 1
    try {
      val client = actorOf(Props(clazz.newInstance))
      // subscribe to Terminated(client)
      watch(client)
      // add the new client to the set of active clients
      val clientId = nextClientId
      clients += clientId  → client      
      // subscribe to POMDPIterations from the Server on this client's behalf
      server.ref ! Subscribe
      sender ! ConfirmClientRegistration(clientId, clazz.getName)
    }
    catch { case _  ⇒ sender ! false }
  }

  def receive = {
    case 'Init  ⇒ sender ! registerHost

    case m: LaunchAgent  ⇒ registerAgent(m.agentType, m.clazz)
    
    case m: LaunchClient  ⇒ registerClient(m.clazz)

    case iteration: POMDPIteration  ⇒ { clients map { _._2 ! iteration }}

    case MessageBundle(content)  ⇒ {
      if (sender == server.ref) content map {
        c  ⇒ agents.find(_._1 == c.agentNumber) map { a  ⇒ a._2._2 ! c }}
    }

    case t: TakeAction  ⇒ {
      if (! (agents contains t.agentNumber)) sender ! PoisonPill
      agents.get(t.agentNumber) map { tuple  ⇒
        if (sender == tuple._2) server.ref forward t else sender ! PoisonPill }
    }

    case Terminated(deceasedActor)  ⇒ {
      if (deceasedActor == server.ref) {
        // the server died
        agents map { a  ⇒ { unwatch(a._2._2); stop(a._2._2) }}
        clients map { c  ⇒ { unwatch(c._2); stop(c._2) }}
        stop(self)
      }
      else {
        agents.find(_._2._2 == deceasedActor) match {
          case Some(deadAgent)  ⇒ {
            // one of this session's agents died
            server.ref ! AgentDied(deadAgent._1)
            agents = agents filterNot { _ == deadAgent }
          }
          case None  ⇒ ()
        }
        clients find (_._2 == deceasedActor) match {
          case Some(deadClient)  ⇒ {
            // one of this session's clients died
            clients = clients filterNot { _ == deadClient }
          }
          case None  ⇒ ()
        }
      }
    }

    case AgentDied(id)  ⇒ {
      // not sure if this alert is beneficial...
      println("An agent on another host (number "+id+") has died.")
    }

    case KillAgent(number)  ⇒ {
      agents filter { _._1 == number } map { a  ⇒ {
        val ref = a._2._2
        ref ! Kill
        self ! Terminated(ref)
      }}
    }
 
    case KillClient(number)  ⇒ {
      clients filter { _._1 == number } map { c  ⇒ {
        val ref = c._2
        ref ! Kill
        self ! Terminated(ref)
      }}
    }
 
    case _  ⇒ () // ignore unhandled messages

  }
}

object Session {
  sealed case class LaunchAgent(agentType: AgentType, clazz: java.lang.Class[_ <: Agent])
  sealed case class LaunchClient(clazz: java.lang.Class[_ <: IterationClient])
  sealed case class KillAgent(number: Int)
  sealed case class KillClient(number: Int)
}