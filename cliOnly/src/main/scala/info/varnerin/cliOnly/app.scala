package info.varnerin.cliOnly

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {

  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props[HelloActor], "supervisor")

  actor ! Say("sayHi")
  Thread.sleep(1000)
  actor ! PoisonPill
  system.terminate()
}

class HelloActor extends Actor with ActorLogging {
  log.info("system started")

  override def postStop(): Unit = {
    log.info("system stopped")
  }

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