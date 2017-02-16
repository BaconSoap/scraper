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
    sql"""SELECT * FROM watched_urls
          WHERE date_last_failed IS NULL AND
           ((date_last_scraped < (NOW() - INTERVAL '1 minute') AND parent_watched_url_id IS NULL)
          OR NOT EXISTS(SELECT 1 FROM scrape_results WHERE watched_url_id = watched_urls.id))""".map(parseWatchedUrl).list().apply().filterNot(w => isBlockedHost(w.url))
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

  def createWatchedUrls(urls: Seq[URL], parentWatchedUrl: WatchedUrl)(implicit session: DBSession = AutoSession): Unit = {
    urls.foreach(createWatchedUrl(_, parentWatchedUrl))
  }

  def createWatchedUrl(url: URL, parentWatchedUrl: WatchedUrl)(implicit session: DBSession = AutoSession): Unit = {
    if (isBlockedHost(url)) return

    sql"""
          INSERT INTO watched_urls (url, user_id, link_matcher, date_last_scraped, parent_watched_url_id)
          SELECT
            ${url.toString},
            ${parentWatchedUrl.userId},
            (SELECT link_matcher FROM urls_to_link_matchers WHERE ${url.toString} LIKE urls_to_link_matchers.url_matcher LIMIT 1),
            NOW() - INTERVAL '1 second',
            ${parentWatchedUrl.id}
          WHERE NOT EXISTS (SELECT 1 FROM watched_urls WHERE url = ${url.toString})
       """.update().apply()
  }

  def isBlockedHost(url: URL): Boolean = {
    Seq("www.linkedin.com", "blog.marcocantu.com", "gufoe.it", "www.nytimes.com").contains(url.getHost)
  }

  def getLinkMatcherForUrl(url: URL)(implicit session: DBSession = ReadOnlyAutoSession): Option[String] = {
    val res = sql"SELECT link_matcher FROM urls_to_link_matchers WHERE ${url.toString} LIKE url_matcher LIMIT 1".map(_.string("link_matcher")).list().apply()
    res match {
      case Nil => None
      case first :: _ => Some(first)
    }
  }

  def markWatchedUrlFailed(watchedUrl: WatchedUrl)(implicit session: DBSession = AutoSession): Unit = {
    sql"UPDATE watched_urls SET date_last_failed = NOW() WHERE id=${watchedUrl.id}".update().apply()
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
