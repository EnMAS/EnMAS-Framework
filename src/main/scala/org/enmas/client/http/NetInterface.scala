package org.enmas.client.http

import org.enmas.pomdp._, org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.util.voodoo.ClassLoaderUtils._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._, akka.pattern.ask,
       dispatch._, javax.servlet._,
       unfiltered.request._, unfiltered.response._, unfiltered.netty._,
       java.net.InetAddress

class NetInterface(application: ActorRef) extends Actor {
  import ClientManager._

  val nothing: PartialFunction[Any, Unit] = { case _  ⇒ () }

  def receive = {
    case NetInterface.Init  ⇒ {
      context become nothing // hence replies only to lifecycle messages
      init
    }
    case _  ⇒ ()
  }

  object ScanHandler extends unfiltered.filter.async.Plan {
    def intent = { case req@Path(Seg("scan" :: address :: Nil)) => {
      (application ? ScanHost(address)) onSuccess {
        case reply: DiscoveryReply  ⇒ {
          req.respond(ResponseString(reply.servers.size + " servers found at "+address+"."))
        }
      } onFailure {
        case _  ⇒ req.respond(ResponseString("The requested host could not be contacted."))
      }
    }}
  }

  def init = {
    unfiltered.jetty.Http(8080).filter(ScanHandler).run
  }

}

object NetInterface {
  case object Init
}