package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.util.EncryptionUtils._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._,
       java.security._, java.security.interfaces._,
       javax.crypto._,
       java.net.InetAddress._,
       org.enmas.examples.Simple._ // for testing only

class ClientManager extends Actor {
  val hostname = getLocalHost.getHostName
  private var uniqueID = 0
  private val keyPair = genKeyPair
  private var server: ActorRef = null
  private var serverPubKey: PublicKey = null
  private var sharedKey: Array[Byte] = null
  private var agents = Map[Int, ActorRef]()


  def registerHost(clientHost: String, clientPort: Int, serverHost: String, serverPort: Int): Boolean = {
    server = remote.actorFor("EnMAS-service", serverHost, serverPort)
    (server ? RegisterHost("EnMAS-client", clientHost, clientPort, keyPair.getPublic)).onException {
      case t: Throwable  ⇒ println(t.getClass.getName)
    }.as[Message].get match {
      case confirmation: ConfirmHostRegistration  ⇒ {
        uniqueID = confirmation.id
        serverPubKey = confirmation.serverPublicKey
        sharedKey = asymEncrypt(serverPubKey,
          asymEncrypt(keyPair.getPrivate,confirmation.encryptedSharedKey))
      }
      case _  ⇒ ()
    }
    serverPubKey != null && sharedKey != null
  }


  def registerAgent(agentType: AgentType): Boolean = {
    (server ? RegisterAgent(uniqueID, agentType)).onException {
      case t: Throwable  ⇒ println(t.getClass.getName)
    }.as[Message].get match {
      case confirmation: ConfirmAgentRegistration  ⇒ {
        val client = actorOf(new myAgent repliesTo self)
        client setId confirmation.agentNumber.toString
        agents += (confirmation.agentNumber  → client)
        self link client
        client.start forward confirmation
        true
      }
      case _  ⇒ false
    }
  }


  def receive = {
    case m: ClientManager.Init  ⇒
      self.channel ! registerHost(m.clientHost, m.clientPort, m.serverHost, m.serverPort)

    case m: ClientManager.LaunchAgent  ⇒ self.channel ! registerAgent(m.agentType)

    case MessageBundle(content)  ⇒ content map { c  ⇒
        agents.find(_._2.getId == c.agentNumber.toString) map { a  ⇒ a._2.forward(c) }}

    case t: TakeAction  ⇒ {
      if (! (agents contains t.agentID)) self.channel ! Kill
      agents.get(t.agentID) map { a  ⇒
        self.sender map { s  ⇒ if (s == a) server forward t else s ! Kill }}}

    case a: AnyRef  ⇒ println(a.getClass.getName + " \n" + a)
  }
}

object ClientManager extends App {
  import java.util.Scanner

  case class Init(clientHost: String, clientPort: Int, serverHost: String, serverPort: Int)
  case class LaunchAgent(agentType: AgentType)

  val in = new Scanner(System.in)
  print("Local hostname: ")
  val clientHost = in.next.trim
  print("Listen on port: ")
  val clientPort = in.nextInt
  print("Server host: ")
  val serverHost = in.next.trim
  print("Server port: ")
  val serverPort = in.nextInt

  val manager = actorOf[ClientManager]
  remote.start("localhost", clientPort).register("EnMAS-client", manager)

  manager ? Init(clientHost, clientPort, serverHost, serverPort)

  print("Launch client of type: ")
  manager ? LaunchAgent(Symbol(in.next.trim))
}