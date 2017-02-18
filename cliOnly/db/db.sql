-- docker run --name postgres -d postgres
-- CREATE ROLE cli_only_dev WITH LOGIN PASSWORD 'password';
-- CREATE DATABASE cli_only_dev WITH OWNER cli_only_dev;

CREATE TABLE users (
  id SERIAL PRIMARY KEY NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email TEXT UNIQUE NOT NULL,
  date_created TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE watched_urls (
  id SERIAL PRIMARY KEY NOT NULL,
  url VARCHAR(8000) NOT NULL,
  user_id INT NOT NULL REFERENCES users,
  date_created TIMESTAMP NOT NULL DEFAULT NOW()
);

-- add default user
insert into users (first_name, last_name, email) VALUES ('Andrew', 'Varnerin', 'andrew@varnerin.info');

-- add default sites to scrape

insert into watched_urls (url, user_id) VALUES 
  ('https://www.google.com', 1),
  ('https://www.reddit.com/r/aww/comments/5tfr68/when_your_human_works_nights_but_you_love_him_so', 1),
  ('https://www.reddit.com', 1),
  ('https://www.reddit.com/r/Games/', 1),
  ('https://www.reddit.com/r/Programming/', 1),
  ('https://www.reddit.com/r/fifthworldproblems/', 1),
  ('https://varnerin.info', 1);

CREATE TABLE scrape_results (
  id SERIAL PRIMARY KEY NOT NULL,
  watched_url_id INT NOT NULL REFERENCES watched_urls,
  title TEXT NOT NULL,
  description TEXT NULL,
  date_created TIMESTAMP NOT NULL DEFAULT NOW()
);

-- add a column to track css paths for links
ALTER TABLE watched_urls ADD COLUMN link_matcher TEXT NULL;

-- use reddit's classes for finding links
UPDATE watched_urls SET link_matcher = '.sitetable.linklisting a.title' WHERE id IN (3,4,5,6);

-- add a date_last_scraped so we can schedule scrapings
ALTER TABLE watched_urls ADD COLUMN date_last_scraped TIMESTAMP NULL;
UPDATE watched_urls SET date_last_scraped = NOW();
ALTER TABLE watched_urls ALTER COLUMN date_last_scraped SET NOT NULL;

-- add a parent_watched_url_id column to track where a url came from
ALTER TABLE watched_urls ADD COLUMN parent_watched_url_id INT NULL REFERENCES watched_urls;

-- add a table to store matchers for certain site patterns
CREATE TABLE urls_to_link_matchers (
  url_matcher TEXT NOT NULL,
  link_matcher TEXT NOT NULL
);

-- add a way for the parser to pick up a link matcher without manual input
INSERT INTO urls_to_link_matchers VALUES ('%reddit.com/r/%', '.sitetable.linklisting a.title');
-- use like:
select link_matcher
  from urls_to_link_matchers
  where 'www.reddit.com/r/boston' like urls_to_link_matchers.url_matcher
  LIMIT 1;

INSERT INTO watched_urls (url, user_id, link_matcher, date_last_scraped)
    VALUES ('https://news.ycombinator.com/', 1, '.storylink', now() - interval '1 day');

-- add a way to mark failed urls so we don't just try them forever and ever
ALTER TABLE watched_urls ADD COLUMN date_last_failed TIMESTAMP NULL;
INSERT INTO watched_urls (url, user_id, date_created, date_last_scraped)
    VALUES ('https://ajgoviwngwgnawoibgb4n.unfo', 1, NOW(), NOW() - INTERVAL '1 day');

-- nytimes
INSERT INTO watched_urls (url, user_id, link_matcher, date_last_scraped)
  VALUES ('https://www.nytimes.com/', 1, '.story-heading a', now() - interval '1 day');

INSERT INTO urls_to_link_matchers VALUES ('%nytimes.com%', '.story-heading a');

-- JUST KIDDING - nytimes has a paywall
delete from scrape_results where watched_url_id in (select id from watched_urls where url like '%nytimes%');
delete from watched_urls where url like '%nytimes%';

-- store html
ALTER TABLE scrape_results ADD COLUMN body TEXT NULL;

-- scrape frequencies and default scrape frequencies
ALTER TABLE watched_urls ADD COLUMN scrape_frequency_minutes INT NOT NULL DEFAULT 60;
UPDATE watched_urls SET scrape_frequency_minutes = 10 WHERE id IN (3,4,5,6, 213, 8);
UPDATE watched_urls SET scrape_frequency_minutes = (60*24) WHERE id IN (1, 7);
ALTER TABLE urls_to_link_matchers ADD COLUMN scrape_frequency_minutes INT NOT NULL DEFAULT 60;
UPDATE urls_to_link_matchers SET scrape_frequency_minutes = 10;

-- add github
INSERT INTO watched_urls (url, user_id, link_matcher, date_last_scraped, scrape_frequency_minutes)
  VALUES ('https://github.com/trending/', 1, '.d-inline-block.col-9.mb-1>h3>a', now() - interval '1 day', 30);
