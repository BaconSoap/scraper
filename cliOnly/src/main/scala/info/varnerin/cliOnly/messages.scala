package info.varnerin.cliOnly

import java.net.URL

import org.jsoup.nodes.Document

case class Start()
case class Scrape(url: String)
case class Say(msg: String)
case class QueueDownload(url: URL)
case class DownloadUrl(url: URL)
case class UrlDownloaded(url: URL, text: Document)
case class ParseHtmlDoc(url: URL, text: Document)
case class HtmlDocParsed(parsed: ParsedUrl)
case class StoreParsedHtml(parsed: ParsedUrl)
case class ParsedUrlStored(url: URL)