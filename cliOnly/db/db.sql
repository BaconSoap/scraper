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