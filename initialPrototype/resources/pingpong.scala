import scala.actors._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._

case class Message(text: String, num: Int)

abstract class PingPongActor extends Actor with App {
	val pingPort = 9000
	val pongPort = 9001
	val delay = 1000
  var num: Int = 0
	classLoader = getClass().getClassLoader() // hack!
	start

	// this method consumes all pending messages
	// the library should have implemented an atomic 
	// receiveAndClear operation
	def clear: Unit = receiveWithin(0) {
		case TIMEOUT => ()
		case _ => clear
	}
}

object Ping extends PingPongActor {

	lazy val pong = select(Node("localhost", pongPort), 'pong)

	def act = {
		alive(pingPort)
		register('ping, self)
		loop {
			pong ! Message("ping", num)
			receiveWithin(delay * 2) {
				case Message(text, n) => {
					println("received: "+text+", "+n)
					num = n + 1
					clear
					Thread.sleep(delay) // wait a while
				}
				case TIMEOUT => println("ping: timed out!")
			}
		}
	}
}

object Pong extends PingPongActor {

	lazy val ping = select(Node("localhost", pingPort), 'ping)

	def act = {
		alive(pongPort)
		register('pong, self)
		loop {
			receiveWithin(delay * 2) {
				case Message(text, n) => {
					println("received: "+text+", "+n)
					num = n + 1
					Thread.sleep(delay) // wait a while
					ping ! Message("pong", num)
					clear
				}
				case TIMEOUT => println("pong: timed out")
			}
		}
	}
}