package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.util.FileUtils._, org.enmas.util.voodoo.ClassLoaderUtils._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._, akka.dispatch._, akka.pattern.ask,
       akka.util.Timeout, akka.util.duration._,
       com.typesafe.config.ConfigFactory

class ClientManager extends Actor with Provisionable {
  import ClientManager._, context._

  private var sessions = List[ActorRef]()
  private var POMDPs = Set[POMDP]()

  private def scanHost(address: String, replyTo: ActorRef) {
    val host = actorFor(
      "akka://enmasServer@"+address+":"+serverPort+"/user/serverManager"
    )
    host ! RequestProvisions
    (host ? Discovery) onSuccess {
//      case reply: DiscoveryReply  ⇒ gui updateServerList reply
      case reply: DiscoveryReply  ⇒ replyTo ! reply
    }
  }

  private def createServer(address: String, pomdp: POMDP, fileData: FileData) {
    val host = actorFor(
      "akka://enmasServer@"+address+":"+serverPort+"/user/serverManager"
    )
    host ! Provision(fileData)
    host ! CreateServerFor(pomdp.getClass.getName)
  }

  private def createSession(server: ServerSpec): Boolean = {
    var result = false
    POMDPs.find( _.getClass.getName == server.pomdpClassName) match {
      case Some(pomdp)  ⇒ {
        val session = actorOf(Props(new Session(server.ref, pomdp)))
        try { Await.result(session ? 'Init, timeout.duration) match {
          case b: Boolean  ⇒ result = b
          case _  ⇒ ()
        }}
        catch { case t: Throwable  ⇒ {
          println("An error occurred while attempting to create a session:")
        }}
      }
      case None  ⇒ ()
    }
    result
  }

  def receive = {

    case Provision(fileData: FileData)  ⇒ {
      val jarOption = provision[POMDP](fileData)
      jarOption map { jar  ⇒ {
        POMDPs ++= findSubclasses[POMDP](jar) filterNot {
          _.getName contains "$"} map { clazz  ⇒ clazz.newInstance }
      }}
    }

    case ScanHost(serverHost)  ⇒ scanHost(serverHost, sender)

    case CreateServer(serverHost, pomdp, fileData)  ⇒ 
      createServer(serverHost, pomdp, fileData)

    case e: Error  ⇒ {
      println(e.cause.getMessage)
      e.cause.printStackTrace
    }

    case m: CreateSession  ⇒ sender ! createSession(m.server)

    case Terminated(deceasedActor)  ⇒ {
      sessions.find(_ == deceasedActor) match { case Some(deadSession)  ⇒ {
          sessions = sessions filterNot { _ == deadSession }
          unwatch(deadSession)
        }
        case None  ⇒ ()
      }
    }

    case error: Throwable  ⇒ { println(
      "Error received from [%s]:\n%s".format(sender, error.getMessage)
    )}

    case _  ⇒ () // ignore unhandled messages
  }
}

object ClientManager extends App {
  // CM specific messages
  sealed case class ScanHost(serverHost: String)
  sealed case class CreateSession(server: ServerSpec)
  sealed case class CreateServer(serverHost: String, pomdp: POMDP, fileData: FileData)

  val system = ActorSystem("enmasClient", ConfigFactory.load.getConfig("enmasClient"))

  implicit val timeout: Timeout = Timeout(3 seconds)

  val manager = system.actorOf(Props[ClientManager], "clientManager")
  val serverPort = 36627 // ENMAS

  import org.enmas.client.gui._, org.enmas.client.http._

  val gui = new ClientGUI(manager)

  val net = system.actorOf(Props(new NetInterface(manager)), "clientNetInterface")
  net ! NetInterface.Init
}