# scraper

A collection of site scrapers, written in scala and using akka.

- `cliOnly` - a self hosted cli app that scrapes links using akka

## libraries

- `akka` for actor model
- `jsoup` for HTML parsing
- `postgresql` for database
- `scalikejdbc` for db querying because I'm old fashioned

## todo

- [ ] handle long-running queues getting downloaded multiple times (because downloading for that host takes so long that the next time the scraper is up it sees links still in the queue as due to be scraped)
- [x] save parse patterns for certain known hosts (to enable automatic following of links without having to manually set up). for instance reddit always uses a certain structure. this would potentially infinitely and recursively add urls to be scanned...
- [ ] alert on hosts that have an abnormal number of failures (linkedin for instance returns 999 for non-browsers ಠ_ಠ
- [x] do... something? with content
  - [x] counting this done because saving *is* doing something, even if it's not something that useful right now...
- [ ] add custom matchers to override getting the `description` field (and default patterns for some hosts like for title)
- [x] add better scrape frequency control. probably don't need to scrape my personal site as frequently as reddit
