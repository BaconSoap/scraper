package info.varnerin.cliOnly

import java.net.URL

import akka.actor.{Actor, ActorLogging}

/**
  * downloads URLs and returns data
  */
class DownloaderActor(host: String) extends Actor with ActorLogging {
  log.info(s"creating downloader actor for host $host")
  override def receive: Receive = {
    case DownloadUrl(url) => download(url)
    case _ => ()
  }

  def download(url: URL): Unit = {
    log.info(s"downloading ${url.toString}")

    sender() ! UrlDownloaded(url, "sample text content")
  }
}
