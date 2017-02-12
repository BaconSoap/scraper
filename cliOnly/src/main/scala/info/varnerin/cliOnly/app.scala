package info.varnerin.cliOnly

import akka.actor.{ActorSystem, PoisonPill, Props}

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {

  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props(classOf[SupervisorActor], system), "supervisor")
  actor ! Scrape("https://www.google.com")
  actor ! Scrape("https://www.reddit.com/r/aww/comments/5tfr68/when_your_human_works_nights_but_you_love_him_so")
  actor ! Scrape("https://www.reddit.com")
  actor ! Scrape("https://www.reddit.com/r/Games/")
  actor ! Scrape("https://www.reddit.com/r/Programming/")
  actor ! Scrape("https://www.reddit.com/r/fifthworldproblems/")
  actor ! Scrape("https://varnerin.info")
}
