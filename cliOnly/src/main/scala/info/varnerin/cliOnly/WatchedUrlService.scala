package info.varnerin.cliOnly

import java.net.URL

import scalikejdbc._

/**
  * Created by andrewvarnerin on 2/12/17.
  */
//noinspection RedundantBlock
class WatchedUrlService {

  def parseWatchedUrl(set: WrappedResultSet): WatchedUrl = {
    WatchedUrl(set.int("id"), new URL(set.string("url")), set.int("user_id"), set.stringOpt("link_matcher"))
  }

  def listUrlsForUser(userId: Int)(implicit session: DBSession = ReadOnlyAutoSession): Seq[WatchedUrl] = {
    sql"SELECT * FROM watched_urls".map(parseWatchedUrl).list().apply()
  }

  def saveParsedUrl(parsed: ParsedUrl)(implicit session: DBSession = AutoSession): ParsedUrl = {
    val watchedUrlId = parsed.watchedUrl.id
    val id = sql"""INSERT INTO scrape_results (watched_url_id, title, description)
                   VALUES (${watchedUrlId}, ${parsed.title}, ${parsed.description})""".updateAndReturnGeneratedKey().apply()
    ParsedUrl(Some(id.toInt), parsed.watchedUrl, parsed.title, parsed.description, parsed.dateAccessed)
  }
}

case class WatchedUrl(id: Int, url: URL, userId: Int, linkMatcher: Option[String])
