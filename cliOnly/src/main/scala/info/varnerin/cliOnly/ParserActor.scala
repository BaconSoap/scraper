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

  def parse(watchedUrl: WatchedUrl, text: Document): Unit = {
    val url = watchedUrl.url.toString
    log.info(s"parsing text from url $url ")
    val title = text.title()
    val descNode = text.select("meta[name=description]").first()
    val desc = if (descNode == null) None else Some(descNode.attr("content"))

    sender() ! HtmlDocParsed(ParsedUrl(None, watchedUrl, title, desc, Instant.now))
  }
}

case class ParsedUrl(id: Option[Int], watchedUrl: WatchedUrl, title: String, description: Option[String], dateAccessed: Instant)