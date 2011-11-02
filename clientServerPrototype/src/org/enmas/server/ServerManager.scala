package org.enmas.server

import org.enmas.pomdp._, org.enmas.server.logging._,
       akka.actor._, akka.actor.Actor._

class ServerManager {

  var servers = List[ActorRef]()

  def approveHost(host: Host) = {}

  def removeHost(publicKey: String) = {}

  def createServer(model: POMDP, port: Int) = {
    val newServer = actorOf( new Server(model, port, new FileLogger(model.name+".enmas")) )
    remote.start("localhost", port).register("EnMAS-service", newServer)
    servers ::= newServer
  }

  def stopServer(server: ActorRef): Unit = if (servers contains server) server.stop else ()

}


object ServerManager extends App {

  import org.enmas.examples.Broadcast._,
         java.util.Scanner

  val in = new Scanner(System.in)
  print("Server port: ")

  val manager = new ServerManager
  manager.createServer(broadcastProblem, in.nextInt())

}