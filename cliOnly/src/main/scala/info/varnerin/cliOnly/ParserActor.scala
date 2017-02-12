package info.varnerin.cliOnly

import java.net.URL
import java.time.Instant

import akka.actor.{Actor, ActorLogging}
import org.jsoup.nodes.Document

/**
  * parses HTML data
  */
class ParserActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case ParseHtmlDoc(url, text) => parse(url, text)
  }

  def parse(url: URL, text: Document): Unit = {
    log.info(s"parsing text from url ${url.toString} ")
    val title = text.title()
    val descNode = text.select("meta[name=description]").first()
    val desc = if (descNode == null) None else Some(descNode.attr("content"))

    sender() ! HtmlDocParsed(ParsedUrl(None, url, title, desc, Instant.now))
  }
}

case class ParsedUrl(id: Option[Int], url: URL, title: String, description: Option[String], dateAccessed: Instant)