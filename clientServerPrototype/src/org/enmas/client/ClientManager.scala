package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._,
       org.enmas.util.EncryptionUtils._,
       scala.collection.immutable._,
       akka.actor._, akka.actor.Actor._,
       java.security._, java.security.interfaces._,
       javax.crypto._,
       java.net.InetAddress._

class ClientManager extends Actor {

  val hostname = getLocalHost.getHostName

  private val keyPair = genKeyPair
  private var serverPubKey: PublicKey = null
  private var sharedKey: Array[Byte] = null

  def registerHost(serverHost: String, serverPort: Int): Boolean = {
    val server = remote.actorFor("EnMAS-service", serverHost, serverPort)

    (server ? RegisterHost(hostname, keyPair.getPublic)).onException {
      case t: Throwable  ⇒ println(t.getClass.getName)
    }.as[Message].get match {

      case confirmation: ConfirmHostRegistration  ⇒ {
        serverPubKey = confirmation.serverPublicKey
        // encryptedSharedKey == asymEncrypt[sprv](asymEncrypt[cpub](skey))
        sharedKey = asymEncrypt(serverPubKey,
          asymEncrypt(keyPair.getPrivate,confirmation.encryptedSharedKey))
      }

      case DenyHostRegistration  ⇒ println("Registration request denied")

      case a: AnyRef  ⇒ {
        println(a.getClass.getName)
      }
    }

    serverPubKey != null && sharedKey != null
  }

  def receive = {
    case h: HostInit  ⇒ {
      if (registerHost(h.serverHost, h.serverPort)) println("We get signal!")
      else println("Connection Failed.")
    }
    case _  ⇒ ()
  }

}

case class HostInit(serverHost: String, serverPort: Int)

object ClientManager extends App {
  val serverHost = "localhost"
  val serverPort = 1337

  val manager = actorOf[ClientManager]
  manager.start
//  remote.start("localhost", 1338).register("EnMAS-client", manager)

  manager ! HostInit(serverHost, serverPort)
}