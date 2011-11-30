package org.enmas.server

import org.enmas.pomdp._,
       akka.actor._, akka.actor.Actor._,
       java.net.{ServerSocket, DatagramSocket, InetAddress}, java.io.IOException

object ServerManager extends App {
  import org.enmas.examples.Broadcast._ // for testing only

  val localhost = InetAddress.getLocalHost.getHostAddress
  var servers = List[ActorRef]()
  val initialPort = 36627
  val portPool = (initialPort until initialPort + 32).toList

  availablePort match {
    case Some(freePort)  ⇒ {
      try {
        servers ::= createServer(broadcastProblem, freePort)
        println("Server up and listening on port "+freePort)
      } catch { case cause: Throwable  ⇒ {
          println("Error: failed to launch server")
          cause.printStackTrace
      }}
    }
    case None  ⇒ println("Error: No free ports available")
  }

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
    val service = "EnMAS-service"+port
    val newServer = actorOf(new Server(model, localhost, port, service))
    remote.start(localhost, port).register(service, newServer)
    newServer
  }

  /** Destroys the specified server.
    */
  def stopServer(server: ActorRef): Unit = if (servers contains server) server.stop else ()

  /** Returns an instance of Some[Int] containing a free port from the
    * port pool.  Returns None if no free port can be found.
    */
  private def availablePort: Option[Int] = {

    def isPortAvailable(port: Int): Boolean = {
      var result = false
      try {
        import java.nio.channels.ServerSocketChannel, java.net.InetSocketAddress
        val tempSock = ServerSocketChannel.open.socket
        tempSock.bind(new InetSocketAddress(port))
        result = true
      }
      catch { case _  ⇒ { 
        result = false
      }}
      result
    }

    def availablePortAux(port: Int = initialPort): Option[Int] = {
      if (portPool contains port) {
        println("testing port #"+port)
        if (isPortAvailable(port)) Some(port)
        else availablePortAux(port + 1)
      }
      else None
    }

    availablePortAux()
  }

}