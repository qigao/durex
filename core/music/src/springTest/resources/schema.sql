drop table if exists MUSIC;

create table MUSIC (
  ID varchar(128) not null primary key,
  TITLE varchar(255) not null,
  DESCRIPTION varchar(1024),
  DURATION int,
  SAMPLE_RATE varchar(8),
  BIT_RATE int,
  CHANNELS int default 2,
  MUSIC_TYPE int not null default 0,
  ARTIST_ID varchar(255),
  COVER_ID varchar(128),
  PLAY_ID varchar(128) not null,
  LYRIC_ID varchar(128),
  CREATE_TIME timestamp,
  UPDATE_TIME timestamp,
  DELETE_TIME timestamp,
  DELETED_FLAG int default 0
);
