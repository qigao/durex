package com.github.durex.music.repository;

import static com.github.durex.model.tables.QMusic.MUSIC;
import static com.github.durex.model.tables.QPlaylistMusic.PLAYLIST_MUSIC;

import com.github.durex.model.tables.records.RMusic;
import com.github.durex.music.model.Music;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequestScoped
public class PlayListMusicRepository {
  public static final Condition NOT_DELETED = MUSIC.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  public Flux<Music> listMusicsByPlayListId(@NotNull String playlistId) {
    var eqPlayListID = PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId);
    var eqMusicID = PLAYLIST_MUSIC.MUSIC_ID.eq(MUSIC.ID);
    var joinTables = PLAYLIST_MUSIC.leftJoin(MUSIC);
    var dslJoinSelect =
        dsl.select(MUSIC.fields())
            .from(joinTables.on(eqMusicID))
            .where(eqPlayListID.and(NOT_DELETED))
            .orderBy(PLAYLIST_MUSIC.MUSIC_ORDER);
    return Flux.from(dslJoinSelect)
        .switchIfEmpty(
            Flux.error(new IllegalArgumentException("playlist not exist with id: " + playlistId)))
        .map(r -> r.into(RMusic.class))
        .map(MusicMapper::mapRecordToDto);
  }

  public Flux<Integer> saveMusicsToPlayList(
      @NotNull String playlistId, @NotNull List<Music> musics) {
    var records = PlayListMusicMapper.mapDtoToRecord(musics, playlistId);
    return Flux.from(
        dsl.batch(
            records.stream()
                .map(m -> dsl.insertInto(PLAYLIST_MUSIC).set(m))
                .collect(Collectors.toUnmodifiableList())));
  }

  public Mono<Integer> deleteMusicFromPlayList(
      @NotNull String playlistId, @NotNull String musicId) {
    return Mono.from(
        dsl.deleteFrom(PLAYLIST_MUSIC)
            .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
            .and(PLAYLIST_MUSIC.MUSIC_ID.eq(musicId)));
  }

  public Mono<Integer> deleteMusicFromPlayList(@NotNull String playlistId, List<String> musicId) {
    return Mono.from(
        dsl.deleteFrom(PLAYLIST_MUSIC)
            .where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId))
            .and(PLAYLIST_MUSIC.MUSIC_ID.in(musicId)));
  }

  public Mono<Integer> clearMusicsFromPlayList(@NotNull String playlistId) {
    return Mono.from(
        dsl.deleteFrom(PLAYLIST_MUSIC).where(PLAYLIST_MUSIC.PLAYLIST_ID.eq(playlistId)));
  }
}
