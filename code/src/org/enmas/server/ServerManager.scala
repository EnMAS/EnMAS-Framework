package org.enmas.server

import org.enmas.pomdp._, org.enmas.messaging._,
       akka.actor._, akka.actor.Actor._,
       com.typesafe.config.ConfigFactory

class ServerManager extends Actor {
  import ServerManager._

  var servers = List[ServerSpec]()

  /** Returns an akka.actor.ActorRef corresponding to a new Server
    * simulating the specified model and listening on the specified port.
    */
  def createServer(model: POMDP): ServerSpec = {
    val ref = system.actorOf(Props(new Server(model)))
    ServerSpec(ref, model)
  }

  /** Destroys the specified server.
    */
  def stopServer(ref: ActorRef) {
    servers filter { _.ref == ref } map { _.ref ! PoisonPill }
    servers = servers filterNot { _.ref == ref }
  }

  def receive = {
    case Discovery  ⇒ sender ! DiscoveryReply(servers)
    case CreateServerFor(model)  ⇒ servers ::= createServer(model)
    case _  ⇒ ()
  }
}

object ServerManager extends App {
  case class CreateServerFor(model: POMDP)
  val system = ActorSystem("enmasServer", ConfigFactory.load.getConfig("enmasServer"))
  val manager = system.actorOf(Props[ServerManager], "serverManager")

  import org.enmas.examples.Broadcast._ // for testing only
  manager ! CreateServerFor(broadcastProblem)
}