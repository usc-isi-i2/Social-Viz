CREATE TABLE health_tweets(entities_hashtags VARCHAR(2000), created_at date, user_id long, user_screen_name varchar(50));

LOAD DATA INFILE 'C:/Temp/data-health_0-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_1-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_2-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_3-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_4-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_5-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_6-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_7-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_8-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_9-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_10-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_11-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_12-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_13-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_14-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_15-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_16-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_17-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_18-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

LOAD DATA INFILE 'C:/Temp/data-health_19-csv.csv' INTO TABLE health_tweets
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

select distinct created_at from health_tweets order by created_at;
select count(distinct created_at) from health_tweets;
select count(*) from health_tweets;

drop table health_tweets;
