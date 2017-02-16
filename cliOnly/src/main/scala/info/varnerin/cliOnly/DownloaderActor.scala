package info.varnerin.cliOnly

import java.net.{URL, UnknownHostException}
import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.util.Timeout
import org.jsoup.{HttpStatusException, Jsoup}

import scala.collection.mutable
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * downloads URLs and returns data
  */
class DownloaderActor(supervisor: ActorRef, host: String) extends Actor with ActorLogging {
  log.info(s"creating downloader actor for host $host")
  val queue: mutable.Queue[WatchedUrl] = mutable.Queue.empty[WatchedUrl]

  def processQueue(): Unit = {
    if (queue.nonEmpty) {
      val watchedUrl = queue.dequeue()
      try {
        download(watchedUrl)
      } catch {
        case _: HttpStatusException => supervisor ! SaveFailedUrl(watchedUrl)
        case _: UnknownHostException => supervisor ! SaveFailedUrl(watchedUrl)
      }
    }
  }

  implicit val timeout = Timeout(3000 millis)
  val timer: Cancellable = context.system.scheduler.schedule(0 millis, 1000 millis, self, "process")

  override def receive: Receive = {
    case QueueDownload(url) => queue.enqueue(url)
    case "process" => processQueue()
    case _ => ()
  }

  def download(watchedUrl: WatchedUrl): Unit = {
    val url = watchedUrl.url.toString
    log.info(s"downloading $url")
    val raw = Jsoup.connect(url).ignoreContentType(true).userAgent("info.varnerin.cliOnly").execute()
    val contentType = raw.contentType()

    // if the doc can't be parsed (generally because it is an image) still store a parse attempt to prevent repeatedly
    // scanning it
    if (!isParseable(contentType)) {
      supervisor ! HtmlDocParsed(ParsedUrl(None, watchedUrl, "[UNPARSEABLE]", None, Instant.now(), Seq.empty[URL], None))
    } else {
      val downloaded = raw.parse()
      supervisor ! UrlDownloaded(watchedUrl, downloaded)
    }
  }

  private def isParseable(contentType: String) = {
    contentType.startsWith("text/") || Seq("application/xml", "application/xhtml+xml").contains(contentType)
  }
}
