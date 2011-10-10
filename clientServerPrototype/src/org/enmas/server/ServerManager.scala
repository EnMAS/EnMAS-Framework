package org.enmas.server

import org.enmas.pomdp._, org.enmas.server.logging._,
       akka.actor._, akka.actor.Actor._

class ServerManager {

  var servers = List[ActorRef]()

  def approveHost(host: Host) = {}

  def removeHost(publicKey: String) = {}

  def createServer(model: POMDP, port: Int) = {
    servers ::= actorOf( new Server(model, port, new FileLogger(model.name+".enmas")) )
  }

  def stopServer(server: ActorRef): Unit = if (servers contains server) server.stop else ()

}

object ServerManager {
  def main(args: Array[String]): Unit = new ServerManager
}