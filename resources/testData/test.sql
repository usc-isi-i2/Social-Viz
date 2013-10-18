CREATE TABLE health_tweets_test(entities_hashtags VARCHAR(2000), created_at date, user_id double, user_screen_name varchar(50));

LOAD DATA INFILE 'C:/Temp/test.csv' INTO TABLE health_tweets_test
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;
