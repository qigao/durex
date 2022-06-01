package com.github.durex.music.repository;

import static com.github.durex.api.tables.QMusic.MUSIC;
import static com.github.durex.api.tables.QPlaylistMusic.PLAYLIST_MUSIC;

import com.github.durex.api.tables.records.RMusic;
import com.github.durex.music.api.Music;
import com.github.durex.music.mapper.MusicMapper;
import com.github.durex.music.mapper.PlayListMusicMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;

@Slf4j
@RequestScoped
public class PlayListMusicRepository {
  public static final Condition NOT_DELETED = MUSIC.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  public List<Music> listMusicsByPlayListId(@NotNull String playlistId) {
    return dsl
        .select(MUSIC.fields())
        .from(PLAYLIST_MUSIC.leftJoin(MUSIC).on(PLAYLIST_MUSIC.MUSIC_ID.eq(MUSIC.ID)))
        .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId).and(NOT_DELETED))
        .orderBy(PLAYLIST_MUSIC.MUSIC_ORDER)
        .fetchInto(RMusic.class)
        .stream()
        .map(MusicMapper::mapRecordToDto)
        .collect(Collectors.toList());
  }

  public int[] saveMusicsToPlayList(@NotNull String playlistId, @NotNull List<Music> musics) {
    var records = PlayListMusicMapper.mapDtoToRecord(musics, playlistId);
    return dsl.batchInsert(records).execute();
  }

  public int deleteMusicFromPlayList(@NotNull String playlistId, @NotNull String musicId) {
    return dsl.deleteFrom(PLAYLIST_MUSIC)
        .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .and(PLAYLIST_MUSIC.MUSIC_ID.eq(musicId))
        .execute();
  }

  public int deleteMusicFromPlayList(@NotNull String playlistId, List<String> musicId) {
    var crud = dsl.deleteFrom(PLAYLIST_MUSIC);
    return crud.where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .and(PLAYLIST_MUSIC.MUSIC_ID.in(musicId))
        .execute();
  }

  public int clearMusicsFromPlayList(@NotNull String playlistId) {
    return dsl.deleteFrom(PLAYLIST_MUSIC)
        .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .execute();
  }
}
