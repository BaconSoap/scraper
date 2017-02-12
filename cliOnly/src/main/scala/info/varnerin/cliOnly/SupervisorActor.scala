package info.varnerin.cliOnly

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

/**
  * top level actor
  */
class SupervisorActor(system: ActorSystem) extends Actor with ActorLogging {
  log.info("supervisor started")
  var downloaders = Map.empty[String, ActorRef]

  override def postStop(): Unit = log.info("supervisor stopped")
  override def receive: Receive = {
    case Scrape(url) => scrape(url)
    case UrlDownloaded(url: URL, text: String) => {
      val parser = system.actorOf(Props[ParserActor])
      parser ! ParseHtmlDoc(url, text)
    }
    case HtmlDocParsed(parsed) => {
      val saver = system.actorOf(Props[StorageActor])
      saver ! StoreParsedHtml(parsed)
    }
    case _ => ()
  }

  def scrape(urlStr: String): Unit = {
    val url = new URL(urlStr)
    val host = url.getHost()
    val actor = downloaders.getOrElse(host, {
      val actor = system.actorOf(Props(new DownloaderActor(host)))
      downloaders += (host -> actor)
      actor
    })
    actor ! DownloadUrl(url)
  }
}
