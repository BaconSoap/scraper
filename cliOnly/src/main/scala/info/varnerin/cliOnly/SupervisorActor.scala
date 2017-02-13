package info.varnerin.cliOnly

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.util.Timeout
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * top level actor
  */
class SupervisorActor(system: ActorSystem) extends Actor with ActorLogging {
  log.info("supervisor started")
  var downloaders = Map.empty[String, ActorRef]
  var urlsOut = 0

  val parserName: Iterator[String] = Iterator from 1 map (i => s"parser-$i")
  val saverName: Iterator[String] = Iterator from 1 map (i => s"saver-$i")

  implicit val timoeout = Timeout(3000 millis)
  val timer: Cancellable = context.system.scheduler.schedule(0 millis, 30 seconds, self, FindAndSendScrapeMessages())

  override def postStop(): Unit = log.info("supervisor stopped")

  def findAndSendScrapeMessages(): Unit = {
    log.info("checking for urls to scrape")

    val svc = new WatchedUrlService()
    val urls = svc.listUrlsForUserToBeScraped(1)

    log.info(s"found ${urls.length} URLs to scrape")

    for (url <- urls) self ! Scrape(url)
  }

  override def receive: Receive = {
    case Scrape(url) => scrape(url)
    case UrlDownloaded(url, text) => {
      val parser = context.actorOf(Props[ParserActor], parserName.next())
      parser ! ParseHtmlDoc(url, text)
    }
    case HtmlDocParsed(parsed) => {
      val saver = context.actorOf(Props[StorageActor], saverName.next())
      saver ! StoreParsedHtml(parsed)
    }
    case ParsedUrlStored(_) => {
      urlsOut -= 1
    }
    case FindAndSendScrapeMessages() => findAndSendScrapeMessages()
  }

  def scrape(watchedUrl: WatchedUrl): Unit = {
    urlsOut += 1
    val host = watchedUrl.url.getHost
    val actor = downloaders.getOrElse(host, {
      val actor = context.actorOf(Props(classOf[DownloaderActor], self, host), host)
      downloaders += (host -> actor)
      actor
    })
    actor ! QueueDownload(watchedUrl)
  }

  case class FindAndSendScrapeMessages()
}
