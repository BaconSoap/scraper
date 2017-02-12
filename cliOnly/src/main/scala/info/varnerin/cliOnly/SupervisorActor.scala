package info.varnerin.cliOnly

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.jsoup.nodes.Document

/**
  * top level actor
  */
class SupervisorActor(system: ActorSystem) extends Actor with ActorLogging {
  log.info("supervisor started")
  var downloaders = Map.empty[String, ActorRef]
  var urlsOut = 0

  val parserName: Iterator[String] = Iterator from 1 map (i => s"parser-$i")
  val saverName: Iterator[String] = Iterator from 1 map (i => s"saver-$i")

  override def postStop(): Unit = log.info("supervisor stopped")
  override def receive: Receive = {
    case Scrape(url) => scrape(url)
    case UrlDownloaded(url: URL, text: Document) => {
      val parser = system.actorOf(Props[ParserActor], parserName.next())
      parser ! ParseHtmlDoc(url, text)
    }
    case HtmlDocParsed(parsed) => {
      val saver = system.actorOf(Props[StorageActor], saverName.next())
      saver ! StoreParsedHtml(parsed)
    }
    case ParsedUrlStored(_) => {
      urlsOut -= 1
      if (urlsOut == 0) system.terminate()
    }
  }

  def scrape(url: URL): Unit = {
    urlsOut += 1
    val host = url.getHost
    val actor = downloaders.getOrElse(host, {
      val actor = system.actorOf(Props(classOf[DownloaderActor], self, host), host)
      downloaders += (host -> actor)
      actor
    })
    actor ! QueueDownload(url)
  }
}
