// server code
class HelloWorldActor extends Actor {
  def receive = {
    case msg => self reply (msg + " World")
  }
}
remote.start("localhost", 2552).register("hello-service", actorOf[HelloWorldActor])

// client code
val actor = remote.actorFor("hello-service", "localhost", 2552)
val result = (actor ? "Hello").as[String]