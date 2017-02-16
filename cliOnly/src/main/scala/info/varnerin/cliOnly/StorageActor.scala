package info.varnerin.cliOnly

import akka.actor.{Actor, ActorLogging}

/**
  * saves parsed data
  */
class StorageActor extends Actor with ActorLogging {


  override def receive: Receive = {
    case StoreParsedHtml(parsed) => save(parsed)
    case SaveFailedUrl(failed) => saveFailed(failed)
  }

  def save(parsed: ParsedUrl): Unit = {
    log.info(s"saving result of ${parsed.watchedUrl.toString}, with title ${parsed.title}")
    val svc = new WatchedUrlService()
    val saved = svc.saveParsedUrl(parsed)
    svc.updateDateLastParsed(parsed.watchedUrl)
    parsed.links match {
      case Nil => ()
      case _ => {
        log.info(s"storing ${parsed.links.length} matched links for ${parsed.watchedUrl.url.toString}")
        svc.createWatchedUrls(parsed.links, parsed.watchedUrl)
      }
    }
    sender() ! ParsedUrlStored(saved.watchedUrl)
  }

  def saveFailed(failed: WatchedUrl): Unit = {
    log.info(s"marking ${failed.url.toString} as a failed url")
    val svc = new WatchedUrlService()
    svc.markWatchedUrlFailed(failed)
  }

}
