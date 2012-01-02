package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.server.ServerSpec, org.enmas.client.gui._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._, akka.dispatch._,
       akka.util.Timeout, akka.util.duration._,
       com.typesafe.config.ConfigFactory,
       java.net.{InetAddress, ServerSocket}

class ClientManager extends Actor {
  import ClientManager._, context._

  private var uniqueID = -1
  private var server: ActorRef = null
  private var agents = Map[Int, (AgentType, ActorRef)]()

  private def scanHost(address: String): ScanResult = {
    val host = actorFor("akka://enmasServer@"+address+":"+serverPort+"/user/serverManager")
    var serverList = List[ServerSpec]()
    try { serverList = Await.result(host ? Discovery, waitingPeriod) match {
      case DiscoveryReply(servers)  ⇒ servers
      case _  ⇒ List[ServerSpec]()
    }}
    catch { case t: Throwable  ⇒ { t.printStackTrace }}
    ScanResult(serverList)
  }

  /** Returns true iff registering this host with the specified
    * server succeeds.  If registration fails, this client manager
    * kills itself.
    *
    * Reasons that lead to registration failure are varied:
    * server is unreachable, server is not running, request denied,
    * or some other exceptional condition.
    */
  private def registerHost(serverRef: ActorRef): Boolean = {
    try { Await.result(serverRef ? RegisterHost(self), waitingPeriod) match {
      case c: ConfirmHostRegistration  ⇒ {
        uniqueID = c.id
        server = serverRef
      }
      case _  ⇒ println("Registration denied by server!")
    }}
    catch { case t: Throwable  ⇒ {
      println("An error occurred during registration:")
      t.printStackTrace
    }}
    uniqueID >= 0
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
      server ? RegisterAgent(uniqueID, agentType), waitingPeriod
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
    case ScanHost(serverHost)  ⇒ sender ! scanHost(serverHost)
    case e: Error  ⇒ {
      println(e.cause.getMessage)
      e.cause.printStackTrace
      self ! Kill
    }
    case m: Init  ⇒ sender ! registerHost(m.server)
    case m: LaunchAgent  ⇒ sender ! registerAgent(m.agentType, m.clazz)
    case MessageBundle(content)  ⇒ content map { c  ⇒
      agents.find(_._1 == c.agentNumber) map { a  ⇒ a._2._2.forward(c) }}
    case t: TakeAction  ⇒ {
      if (! (agents contains t.agentNumber)) sender ! Kill
      agents.get(t.agentNumber) map { tuple  ⇒
        if (sender == tuple._2) server forward t else sender ! Kill
      }
    }
    case a: AnyRef  ⇒ println(a.getClass.getName + " \n" + a)
    case _  ⇒ ()
  }
}

object ClientManager extends App {
  // CM specific messages
  sealed case class ScanHost(serverHost: String)
  sealed case class ScanResult(servers: List[ServerSpec])
  sealed case class Init(server: ActorRef)
  sealed case class LaunchAgent(agentType: AgentType, clazz: java.lang.Class[_ <: Agent])

  val system = ActorSystem("enmasClient", ConfigFactory.load.getConfig("enmasClient"))
  val waitingPeriod = 3 seconds
  implicit val timeout: Timeout = Timeout(waitingPeriod)

  val manager = system.actorOf(Props[ClientManager], "clientManager")
  val serverPort = 36627 // ENMAS
  val gui = new ClientGUI(manager)
}