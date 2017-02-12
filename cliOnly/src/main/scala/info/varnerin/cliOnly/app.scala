package info.varnerin.cliOnly

import java.net.URL
import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}

/**
  * Created by andrewvarnerin on 2/11/17.
  */
object app extends App {

  val system = ActorSystem("appSystem")
  val actor = system.actorOf(Props(new SupervisorActor(system)), "supervisor")
  actor ! Scrape("https://www.google.com")
  actor ! Scrape("https://www.reddit.com/r/aww/comments/5tfr68/when_your_human_works_nights_but_you_love_him_so")
  actor ! Scrape("https://www.reddit.com")
  Thread.sleep(5000)
  actor ! PoisonPill
  system.terminate()
}

/**
  * top level actor
  */
class SupervisorActor(system: ActorSystem) extends Actor with ActorLogging {
  log.info("supervisor started")
  var downloaders = Map.empty[String, ActorRef]

  override def postStop(): Unit = log.info("supervisor stopped")
  override def receive: Receive = {
    case Scrape(url) => scrape(url)
    case UrlDownloaded(url: URL, text: String) => {
      val parser = system.actorOf(Props[ParserActor])
      parser ! ParseHtmlDoc(url, text)
    }
    case HtmlDocParsed(parsed) => {
      val saver = system.actorOf(Props[StorageActor])
      saver ! StoreParsedHtml(parsed)
    }
    case _ => ()
  }

  def scrape(urlStr: String): Unit = {
    val url = new URL(urlStr)
    val host = url.getHost()
    val actor = downloaders.getOrElse(host, {
      val actor = system.actorOf(Props(new DownloaderActor(host)))
      downloaders += (host -> actor)
      actor
    })
    actor ! DownloadUrl(url)
  }
}

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

/**
  * saves parsed data
  */
class StorageActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case StoreParsedHtml(parsed) => save(parsed)
  }

  def save(parsed: ParsedUrl): Unit = {
    log.info(s"saving $parsed")
  }
}

case class Start()
case class Scrape(url: String)
case class Say(msg: String)
case class DownloadUrl(url: URL)
case class UrlDownloaded(url: URL, text: String)
case class ParseHtmlDoc(url: URL, text: String)
case class HtmlDocParsed(parsed: ParsedUrl)
case class StoreParsedHtml(parsed: ParsedUrl)

case class ParsedUrl(url: URL, title: String, dateAccessed: Instant)