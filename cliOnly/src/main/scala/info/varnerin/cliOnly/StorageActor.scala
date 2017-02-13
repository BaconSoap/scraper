package info.varnerin.cliOnly

import akka.actor.{Actor, ActorLogging}

/**
  * saves parsed data
  */
class StorageActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case StoreParsedHtml(parsed) => save(parsed)
  }

  def save(parsed: ParsedUrl): Unit = {
    log.info(s"saving $parsed")
    val svc = new WatchedUrlService()
    val saved = svc.saveParsedUrl(parsed)
    svc.updateDateLastParsed(parsed.watchedUrl)
    log.info(s"saved $saved")
    parsed.links match {
      case Nil => ()
      case _ => log.info(s"storing ${parsed.links.length} matched links for ${parsed.watchedUrl.url.toString}")
    }
    sender() ! ParsedUrlStored(saved.watchedUrl)
  }
}
