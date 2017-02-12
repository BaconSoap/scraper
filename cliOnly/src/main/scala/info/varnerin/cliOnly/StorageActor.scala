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
    sender() ! ParsedUrlStored(parsed.url)
  }
}
