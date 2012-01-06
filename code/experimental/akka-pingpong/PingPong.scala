import akka.actor._, akka.actor.Actor._

case object Ping
case object Pong

class Pinger extends Actor {
  def receive = {
    case Pong  ⇒ {
      println("Pinger: Received a Pong!")
      sender ! Ping
    }
  }
}

class Ponger extends Actor {
  context.actorSelection("../Ping*") ! Pong
  def receive = {
    case Ping  ⇒ {
      println("Ponger: Received a Ping!")
      sender ! Pong
    }
  }
}

object PingPong extends App {
  val system = ActorSystem()
  val pinger = system.actorOf(Props[Pinger], "Ping")
  val pinger2 = system.actorOf(Props[Pinger], "Ping2")
  val pinger3 = system.actorOf(Props[Pinger], "Ping3")
  val ponger = system.actorOf(Props[Ponger], "Pong")
}