package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._, akka.actor.Actor._,
       java.net.{ServerSocket, InetAddress}, java.io.IOException

object ServerManager extends App {
  import org.enmas.examples.Broadcast._ // for testing only

  val localhost = InetAddress.getLocalHost.getHostAddress
  var servers = List[ActorRef]()

  /** Adds host to the list of known approved hosts.
    */
  def approveHost(host: Host) = {}

  /** De-authorizes the host identified by the supplied public key.
    */
  def removeHost(publicKey: String) = {}

  /** Returns an akka.actor.ActorRef corresponding to a new Server
    * simulating the specified model and listening on the specified port.
    */
  def createServer(model: POMDP, port: Int): ActorRef = {
    val service = "EnMAS-service"
    val newServer = actorOf(new Server(model, localhost, port, service))
    remote.start(localhost, port).register(service, newServer)
    servers ::= newServer
    println("Server up and listening on port "+port)
    newServer
  }

  /** Destroys the specified server.
    */
  def stopServer(server: ActorRef): Unit = if (servers contains server) server.stop else ()

  def nextServerPort(p: Int = 36627): Option[Int] = {
    if (p < 36659) {
      try {
        (new ServerSocket(p)).close
        Some(p)
      }
      catch { case _  ⇒ nextServerPort(p + 1) }
    }
    else None
  }

  val server = nextServerPort() match {
    case Some(port)  ⇒ createServer(broadcastProblem, port)
    case None  ⇒ println("Error: Failed to launch server.")
  }

}