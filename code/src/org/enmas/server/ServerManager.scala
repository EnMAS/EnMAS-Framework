package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._, akka.actor.Actor._,
       com.typesafe.config.ConfigFactory

class ServerManager extends Actor {
  import ServerManager._

  var servers = List[ServerSpec]()

  /** Returns an akka.actor.ActorRef corresponding to a new Server
    * simulating the specified POMDP and listening on the specified port.
    */
  def createServer(pomdp: POMDP): ActorRef = {
    val ref = system.actorOf(Props(new Server(pomdp)))
    servers ::= ServerSpec(ref, pomdp)
    ref
  }

  /** Destroys the specified server.
    */
  def stopServer(ref: ActorRef) {
    servers filter { _.ref == ref } map { s  ⇒ context stop s.ref }
    servers = servers filterNot { _.ref == ref }
  }

  def receive = {
    case Discovery  ⇒ sender ! DiscoveryReply(servers)
    case CreateServerFor(pomdp)  ⇒ sender ! createServer(pomdp)
    case _  ⇒ ()
  }
}

object ServerManager extends App {
  case class CreateServerFor(pomdp: POMDP)
  val system = ActorSystem("enmasServer", ConfigFactory.load.getConfig("enmasServer"))
  val manager = system.actorOf(Props[ServerManager], "serverManager")

  import org.enmas.examples.Broadcast._, org.enmas.examples.Simple._ // for testing only
  manager ! CreateServerFor(broadcastProblem)
  manager ! CreateServerFor(simpleModel)
}