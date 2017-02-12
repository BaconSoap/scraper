package info.varnerin.cliOnly

import java.net.URL
import java.time.Instant

import akka.actor.{Actor, ActorLogging}

/**
  * parses HTML data
  */
class ParserActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case ParseHtmlDoc(url, text) => parse(url, text)
  }

  def parse(url: URL, text: String): Unit = {
    log.info(s"parsing text from url ${url.toString} ")
    sender() ! HtmlDocParsed(ParsedUrl(url, "test title", Instant.now))
  }
}

case class ParsedUrl(url: URL, title: String, dateAccessed: Instant)