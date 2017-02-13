package info.varnerin.cliOnly

import java.net.URL

import org.jsoup.nodes.Document

case class Start()
case class Scrape(url: WatchedUrl)
case class Say(msg: String)
case class QueueDownload(url: WatchedUrl)
case class DownloadUrl(url: WatchedUrl)
case class UrlDownloaded(url: WatchedUrl, text: Document)
case class ParseHtmlDoc(url: WatchedUrl, text: Document)
case class HtmlDocParsed(parsed: ParsedUrl)
case class StoreParsedHtml(parsed: ParsedUrl)
case class ParsedUrlStored(url: WatchedUrl)