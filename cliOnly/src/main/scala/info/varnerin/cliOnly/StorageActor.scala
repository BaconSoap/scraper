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
    log.info(s"saved $saved")
    sender() ! ParsedUrlStored(saved.url)
  }
}
