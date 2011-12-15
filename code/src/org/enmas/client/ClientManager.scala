package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.util.EncryptionUtils._,
       org.enmas.client.gui._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._,
       java.security._, java.security.interfaces._,
       javax.crypto._,
       java.net.{InetAddress, ServerSocket},
       org.enmas.examples.Broadcast._, org.enmas.examples.JavaBroadcastAgent // for testing only

class ClientManager extends Actor {
  private var uniqueID = 0
  private val keyPair = genKeyPair
  private var server: ActorRef = null
  private var serverPubKey: PublicKey = null
  private var sharedKey: Array[Byte] = null
  private var agents = Map[Int, ActorRef]()

  private def scanHost(serverHost: String, serverPort: Int) =
    (remote.actorFor("EnMAS-service"+serverPort, serverHost, serverPort) ? Discovery).get

  private def scanHost(serverHost: String): ClientManager.ScanResult = {
    val initialPort = 36627
    val portsToScan = (initialPort until initialPort + 32).toList
    val tries = portsToScan map {
      port  ⇒ {
        val remoteActor = remote.actorFor("EnMAS-service"+port, serverHost, port)
        (remoteActor, remoteActor ? Discovery)
      }
    }
    val replies = tries filter { t  ⇒ t match {
      case (remoteActor, future)  ⇒ {
        try { future.get match {
          case reply: DiscoveryReply  ⇒ true
          case _  ⇒ false
        }}
        catch { case _  ⇒ {
          try { remoteActor.stop } catch { case _  ⇒ () }
          false
        }}
      }
      case _  ⇒ false
    }}
    ClientManager.ScanResult(replies map {_._2.get.asInstanceOf[DiscoveryReply]})
  }

  /** Returns true iff registering this host with the specified
    * server succeeds.  If registration fails, this client manager
    * kills itself.
    *
    * Reasons that lead to registration failure are varied:
    * server is unreachable, server is not running, request denied,
    * or some other exceptional condition.
    */
  private def registerHost(clientPort: Int, serverHost: String, serverPort: Int): Boolean = {
    server = remote.actorFor("EnMAS-service"+serverPort, serverHost, serverPort)
    (server ? RegisterHost(
      "EnMAS-client", ClientManager.clientHost, clientPort, keyPair.getPublic)
    ).onException {
      case t: Throwable  ⇒ println(t.getClass.getName)
    }.as[Message].get match {
      case confirmation: ConfirmHostRegistration  ⇒ {
        uniqueID = confirmation.id
        serverPubKey = confirmation.serverPublicKey
        sharedKey = confirmation.encryptedSharedKey
      }
      case _  ⇒ self ! Kill
    }
    serverPubKey != null && sharedKey != null
  }

  /** Returns true iff the server confirms the agent registration
    * and creating the agent succeeds.
    *
    * Upon successful registration, the agent is started and forwarded
    * the ConfirmAgentRegistration from the server.  The agent uses that
    * information for initialization and then become()s its user-defined
    * policy function.
    */
  private def registerAgent(agentType: AgentType, clazz: java.lang.Class[_ <: Agent]): Boolean = {
    try {
      (server ? RegisterAgent(uniqueID, agentType)).onException {
        case t: Throwable  ⇒ println(t.getClass.getName)
      }.as[Message].get match {
        case confirmation: ConfirmAgentRegistration  ⇒ {
          val client = actorOf(clazz.newInstance repliesTo self)
          client setId confirmation.agentNumber.toString
          agents += (confirmation.agentNumber  → client)
          self link client
          client.start forward confirmation
          true
        }
        case _  ⇒ false
      }
    }
    catch { case t: Throwable  ⇒ false }
  }

  def receive = {
    case ClientManager.ScanHost(serverHost)  ⇒ self.channel ! scanHost(serverHost)
    case e: ServerError  ⇒ {
      println(e.cause.getMessage)
      e.cause.printStackTrace
      self ! Kill
    }
    case e: ClientError  ⇒ {
      println(e.cause.getMessage)
      e.cause.printStackTrace
      self ! Kill
    }
    case m: ClientManager.Init  ⇒
      self.channel ! registerHost(m.clientPort, m.serverHost, m.serverPort)
    case m: ClientManager.LaunchAgent  ⇒ self.channel ! registerAgent(m.agentType, m.clazz)
    case MessageBundle(content)  ⇒ content map { c  ⇒
        agents.find(_._2.getId == c.agentNumber.toString) map { a  ⇒ a._2.forward(c) }}
    case t: TakeAction  ⇒ {
      if (! (agents contains t.agentNumber)) self.channel ! Kill
      agents.get(t.agentNumber) map { a  ⇒
        self.sender map { s  ⇒ if (s == a) server forward t else s ! Kill }}}
    case a: AnyRef  ⇒ println(a.getClass.getName + " \n" + a)
  }
}

object ClientManager extends App {
  import java.util.Scanner

  sealed case class ScanHost(serverHost: String)
  sealed case class ScanResult(replies: List[DiscoveryReply])
  sealed case class Init(clientPort: Int, serverHost: String, serverPort: Int)
  sealed case class LaunchAgent(agentType: AgentType, clazz: java.lang.Class[_ <: Agent])

  val clientHost = InetAddress.getLocalHost.getHostAddress
  val manager = actorOf[ClientManager]
  val clientPort = (new ServerSocket(0)).getLocalPort
  remote.start(clientHost, clientPort).register("EnMAS-client", manager)
  println("Listening on port "+clientPort)

  val gui = new ClientGUI(manager)
}