package com.github.durex.music.repository;

import static com.github.durex.model.tables.QMusic.MUSIC;
import static com.github.durex.model.tables.QPlaylistMusic.PLAYLIST_MUSIC;

import com.github.durex.model.tables.records.RMusic;
import com.github.durex.music.mapper.MusicMapper;
import com.github.durex.music.mapper.PlayListMusicMapper;
import com.github.durex.music.model.Music;
import java.util.Arrays;
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

  public List<Integer> saveMusicsToPlayList(
      @NotNull String playlistId, @NotNull List<Music> musics) {
    var records = PlayListMusicMapper.mapDtoToRecord(musics, playlistId);
    var result = dsl.batchInsert(records).execute();
    return Arrays.stream(result).boxed().collect(Collectors.toUnmodifiableList());
  }

  public Integer deleteMusicFromPlayList(@NotNull String playlistId, @NotNull String musicId) {
    return dsl.deleteFrom(PLAYLIST_MUSIC)
        .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .and(PLAYLIST_MUSIC.MUSIC_ID.eq(musicId))
        .execute();
  }

  public Integer deleteMusicFromPlayList(@NotNull String playlistId, List<String> musicId) {
    var crud = dsl.deleteFrom(PLAYLIST_MUSIC);
    return crud.where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .and(PLAYLIST_MUSIC.MUSIC_ID.in(musicId))
        .execute();
  }

  public Integer clearMusicsFromPlayList(@NotNull String playlistId) {
    return dsl.deleteFrom(PLAYLIST_MUSIC)
        .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
        .execute();
  }
}
