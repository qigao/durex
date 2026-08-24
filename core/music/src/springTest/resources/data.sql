insert into MUSIC (
  ID, TITLE, DESCRIPTION, DURATION, SAMPLE_RATE, BIT_RATE, CHANNELS,
  MUSIC_TYPE, ARTIST_ID, COVER_ID, PLAY_ID, LYRIC_ID, DELETED_FLAG
) values (
  'music-1', 'Spring Runtime Song', 'integration-test', 180, '44100', 320, 2,
  0, 'artist-1', 'cover-1', 'play-1', 'lyric-1', 0
);

insert into MUSIC (
  ID, TITLE, DESCRIPTION, DURATION, SAMPLE_RATE, BIT_RATE, CHANNELS,
  MUSIC_TYPE, ARTIST_ID, COVER_ID, PLAY_ID, LYRIC_ID, DELETED_FLAG
) values (
  'music-deleted', 'Deleted Song', 'soft-delete-fixture', 120, '44100', 192, 2,
  0, 'artist-2', 'cover-2', 'play-2', 'lyric-2', 1
);

insert into PLAYLIST (ID, DESCRIPTION, TITLE, COVER_ID, DELETED_FLAG)
values ('playlist-1', 'integration-test', 'Active Playlist', 'cover-1', 0);

insert into PLAYLIST (ID, DESCRIPTION, TITLE, COVER_ID, DELETED_FLAG)
values ('playlist-deleted', 'soft-delete-fixture', 'Deleted Playlist', 'cover-2', 1);
