import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {
  println("scraper started")

  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props[HelloActor])

  actor ! Say("sayHi")
  Thread.sleep(1000)
  println("scraper stopped")
}

class HelloActor extends Actor {
  val log = Logging(context.system, this)

  def receive: PartialFunction[Any, Unit] = {
    case Say(msg) => log.info(s"saying $msg")
    case _ => log.info("unknown saying")
  }
}

case class Say(msg: String)