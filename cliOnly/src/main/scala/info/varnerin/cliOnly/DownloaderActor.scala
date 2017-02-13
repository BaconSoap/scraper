package info.varnerin.cliOnly

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.util.Timeout
import org.jsoup.Jsoup

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
    if (queue.nonEmpty) download(queue.dequeue())
  }

  implicit val timoeout = Timeout(3000 millis)
  val timer: Cancellable = context.system.scheduler.schedule(0 millis, 1000 millis, self, "process")

  override def receive: Receive = {
    case QueueDownload(url) => queue.enqueue(url)
    case "process" => processQueue()
    case _ => ()
  }

  def download(watchedUrl: WatchedUrl): Unit = {
    val url = watchedUrl.url.toString
    log.info(s"downloading $url")
    val downloaded = Jsoup.connect(url).userAgent("info.varnerin.cliOnly").get()
    supervisor ! UrlDownloaded(watchedUrl, downloaded)
  }
}
