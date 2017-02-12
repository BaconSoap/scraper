package info.varnerin.cliOnly

import akka.actor.{ActorSystem, Props}
import scalikejdbc.ConnectionPool
import scalikejdbc._

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {
  ConnectionPool.singleton("jdbc:postgresql://localhost:5432/cli_only_dev", "cli_only_dev", "password")

  implicit val session = ReadOnlyAutoSession
  val urls = sql"SELECT url FROM watched_urls".map(_.string("url")).list().apply()
  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props(classOf[SupervisorActor], system), "supervisor")
  for (url <- urls) actor ! Scrape(url)
}
