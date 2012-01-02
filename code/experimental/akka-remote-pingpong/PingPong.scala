import akka.actor._, akka.actor.Actor._

case object Ping
case object Pong

class PongActor extends Actor {
  def receive = {
    case Ping  ⇒ {
      println(self.path + ": Received Ping!")
      sender ! Pong
    }
    case _  ⇒ ()
  }
}

class PingActor extends Actor {
  context.actorSelection("../Pong*") ! Ping // starts things off

  def receive = {
    case Pong  ⇒ {
      println(self.path + ": Received Pong!")
      sender ! Ping
    }
    case _  ⇒ ()
  }
}

object Ping extends App {
  val system = ActorSystem("Ping")
  system.actorOf(Props[PongActor], name="Ping")
}

object Pong extends App {
  val system = ActorSystem("Pong")
  system.actorOf(Props[PongActor], name="Pong")
}