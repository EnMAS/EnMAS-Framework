package org.enmas.client.http

import org.enmas.pomdp._, org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.util.voodoo.ClassLoaderUtils._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._, akka.pattern.ask,
       unfiltered.request._, unfiltered.response._, unfiltered.netty._

class NetInterface(application: ActorRef) extends Actor {
  import ClientManager._

  def receive = {
    case NetInterface.Init  ⇒ {
      context become { case _  ⇒ () } // now replies only to lifecycle messages
      startHttpServer
    }
    case _  ⇒ ()
  }

  object ScanHandler extends unfiltered.filter.async.Plan {
    def intent = { case req@Path(Seg("scan" :: address :: Nil)) => {
      (application ? ScanHost(address)) onSuccess {
        case reply: DiscoveryReply  ⇒ req respond ResponseString(
          reply.servers.foldLeft("{ servers: [") {
            (s: String, srv: ServerSpec)  ⇒ s + "\"%s\", ".format(srv)
          }.stripSuffix(", ") + "] }")
      } onFailure {
        case _  ⇒ req respond ResponseString(
          "{ error: \"The specified host could not be contacted.\" }"
        )
      }
    }}
  }

  /** Starts the HTTP server.
    */
  def startHttpServer = unfiltered.jetty.Http(8080).filter(ScanHandler).run
}

object NetInterface { case object Init }