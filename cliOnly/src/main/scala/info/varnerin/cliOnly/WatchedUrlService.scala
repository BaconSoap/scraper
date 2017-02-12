package info.varnerin.cliOnly

import scalikejdbc._

/**
  * Created by andrewvarnerin on 2/12/17.
  */
class WatchedUrlService {
  def listUrlsForUser(userId: Int)(implicit session: DBSession = ReadOnlyAutoSession): Seq[String] = {
    sql"SELECT url FROM watched_urls".map(_.string("url")).list().apply()
  }
}
