import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
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

class HelloActor extends Actor with ActorLogging {
  def receive: PartialFunction[Any, Unit] = {
    case Say(msg) => log.info(s"saying $msg")
    case _ => log.info("unknown saying")
  }
}

/**
  * top level actor
  */
class SupervisorActor extends Actor with ActorLogging {
  override def receive: Receive = ???
}

/**
  * downloads URLs and returns data
  */
class DownloaderActor extends Actor with ActorLogging {
  override def receive: Receive = ???
}

/**
  * parses HTML data
  */
class ParserActor extends Actor with ActorLogging {
  override def receive: Receive = ???
}

/**
  * saves parsed data
  */
class StorageActor extends Actor with ActorLogging {
  override def receive: Receive = ???
}

case class Say(msg: String)