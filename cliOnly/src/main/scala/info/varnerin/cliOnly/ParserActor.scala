package info.varnerin.cliOnly

import java.net.{URI, URL}
import java.time.Instant

import akka.actor.{Actor, ActorLogging}
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

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
    val links = watchedUrl.linkMatcher match {
      case Some(matcher) => findLinks(text, matcher, watchedUrl.url).map(_.url)
      case None => Seq.empty[URL]
    }
    sender() ! HtmlDocParsed(ParsedUrl(None, watchedUrl, title, desc, Instant.now, links))
  }

  def findLinks(text: Document, matcher: String, root: URL): Seq[LinkAndText] = {
    val links = text.select(matcher).iterator().toList
    links.map(e => LinkAndText(getLink(root, e.attr("href")), e.text()))
  }

  def getLink(root: URL, possiblyRelativeUrl: String): URL = {
    if (new URI(possiblyRelativeUrl).isAbsolute)
      new URL(possiblyRelativeUrl)
    else
      new URL(root, possiblyRelativeUrl)
  }

  case class LinkAndText(url: URL, text: String)
}

case class ParsedUrl(id: Option[Int], watchedUrl: WatchedUrl, title: String, description: Option[String], dateAccessed: Instant, links: Seq[URL])