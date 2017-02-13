package info.varnerin.cliOnly

import akka.actor.{ActorSystem, Props}
import scalikejdbc.ConnectionPool

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/cli_only_dev", "cli_only_dev", "password")

  val svc = new WatchedUrlService()
  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props(classOf[SupervisorActor], system), "supervisor")
}
