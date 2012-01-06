package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.server.ServerSpec, org.enmas.client.gui._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._, akka.dispatch._,
       akka.util.Timeout, akka.util.duration._,
       com.typesafe.config.ConfigFactory

class ClientManager extends Actor {
  import ClientManager._, context._

  private var sessions = List[ActorRef]()

  private def scanHost(address: String): ScanResult = {
    val host = actorFor("akka://enmasServer@"+address+":"+serverPort+"/user/serverManager")
    var serverList = List[ServerSpec]()
    try { serverList = Await.result(host ? Discovery, timeout.duration) match {
      case DiscoveryReply(servers)  ⇒ servers
      case _  ⇒ List[ServerSpec]()
    }}
    catch { case t: Throwable  ⇒ { t.printStackTrace }}
    ScanResult(serverList)
  }

  private def createSession(server: ActorRef): Boolean = {
    var result = false
    val session = actorOf(Props(new Session(server)))
    try { Await.result(session ? 'Init, timeout.duration) match {
      case b: Boolean  ⇒ result = b
      case _  ⇒ ()
    }}
    catch { case t: Throwable  ⇒ {
      println("An error occurred while attempting to create a session:")
      t.printStackTrace
    }}
    result
  }

  def receive = {
    case ScanHost(serverHost)  ⇒ sender ! scanHost(serverHost)

    case e: Error  ⇒ {
      println(e.cause.getMessage)
      e.cause.printStackTrace
    }

    case m: CreateSession  ⇒ sender ! createSession(m.server)

    case Terminated(deceasedActor)  ⇒ {
      sessions.find(_ == deceasedActor) match { case Some(deadSession)  ⇒ {
          println("A session died! Nuts...")
          sessions = sessions filterNot { _ == deadSession }
          unwatch(deadSession)
        }
        case None  ⇒ ()
      }
    }

    case _  ⇒ () // ignore unhandled messages
  }
}

object ClientManager extends App {
  // CM specific messages
  sealed case class ScanHost(serverHost: String)
  sealed case class ScanResult(servers: List[ServerSpec])
  sealed case class CreateSession(server: ActorRef)
  sealed case class LaunchAgent(agentType: AgentType, clazz: java.lang.Class[_ <: Agent])

  val system = ActorSystem("enmasClient", ConfigFactory.load.getConfig("enmasClient"))
  implicit val timeout: Timeout = Timeout(3 seconds)

  val manager = system.actorOf(Props[ClientManager], "clientManager")
  val serverPort = 36627 // ENMAS
  val gui = new ClientGUI(manager)
}