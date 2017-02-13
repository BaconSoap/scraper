package info.varnerin.cliOnly

import java.net.URL

import scalikejdbc._

/**
  * Created by andrewvarnerin on 2/12/17.
  */
//noinspection RedundantBlock
class WatchedUrlService {

  private def parseWatchedUrl(set: WrappedResultSet): WatchedUrl = {
    WatchedUrl(set.int("id"), new URL(set.string("url")), set.int("user_id"), set.stringOpt("link_matcher"), set.intOpt("parent_watched_url_id"))
  }

  def listUrlsForUser(userId: Int)(implicit session: DBSession = ReadOnlyAutoSession): Seq[WatchedUrl] = {
    sql"SELECT * FROM watched_urls".map(parseWatchedUrl).list().apply()
  }

  /**
    * get WatchedUrls that are due for scraping
    * @param userId the user to filter down to
    * @param session transaction session
    * @return a list of URLs that are ready to be scraped
    */
  def listUrlsForUserToBeScraped(userId: Int)(implicit session: DBSession = ReadOnlyAutoSession): Seq[WatchedUrl] = {
    sql"SELECT * FROM watched_urls WHERE date_last_scraped < (NOW() - INTERVAL '1 minute')".map(parseWatchedUrl).list().apply()
  }

  def saveParsedUrl(parsed: ParsedUrl)(implicit session: DBSession = AutoSession): ParsedUrl = {
    val watchedUrlId = parsed.watchedUrl.id
    val id = sql"""INSERT INTO scrape_results (watched_url_id, title, description)
                   VALUES (${watchedUrlId}, ${parsed.title}, ${parsed.description})""".updateAndReturnGeneratedKey().apply()
    ParsedUrl(Some(id.toInt), parsed.watchedUrl, parsed.title, parsed.description, parsed.dateAccessed, parsed.links)
  }

  def updateDateLastParsed(watchedUrl: WatchedUrl)(implicit session: DBSession = AutoSession): Unit = {
    sql"UPDATE watched_urls SET date_last_scraped = NOW() WHERE id = ${watchedUrl.id}".update().apply()
  }
}

/**
  * Represents a given URL to be scraped. URLs are owned by a user, and can have both a parent URL and a selector for
  * finding new URLs to scrape.
  * @param id pk
  * @param url fully specified URL to scrape
  * @param userId the user that owns the URL
  * @param linkMatcher a CSS selector to find more links. these links will then be scraped
  */
case class WatchedUrl(id: Int, url: URL, userId: Int, linkMatcher: Option[String], parentId: Option[Int])
