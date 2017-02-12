package info.varnerin.cliOnly

import java.net.URL

case class Start()
case class Scrape(url: String)
case class Say(msg: String)
case class DownloadUrl(url: URL)
case class UrlDownloaded(url: URL, text: String)
case class ParseHtmlDoc(url: URL, text: String)
case class HtmlDocParsed(parsed: ParsedUrl)
case class StoreParsedHtml(parsed: ParsedUrl)
