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

object PingPong extends App {
  val system = ActorSystem()
  system.actorOf(Props[PongActor], name="Pong")
  system.actorOf(Props[PingActor], name="Ping")
}

object PingPongPong extends App {
  val system = ActorSystem()
  system.actorOf(Props[PongActor], name="Pong1")
  system.actorOf(Props[PongActor], name="Pong2")
  system.actorOf(Props[PingActor], name="Ping")
}