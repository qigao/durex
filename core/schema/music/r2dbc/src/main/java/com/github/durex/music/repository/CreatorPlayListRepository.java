package com.github.durex.music.repository;

import static com.github.durex.model.tables.QCreatorPlaylist.CREATOR_PLAYLIST;
import static com.github.durex.model.tables.QPlaylist.PLAYLIST;

import com.github.durex.model.tables.records.RPlaylist;
import com.github.durex.music.mapper.PlayListMapper;
import com.github.durex.music.model.PlayList;
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
public class CreatorPlayListRepository {
  public static final Condition DELETED_FLAG = PLAYLIST.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  public Mono<Integer> savePlaylistToCreator(
      @NotNull String creatorId, @NotNull String playlistId) {
    return Mono.from(
        dsl.insertInto(CREATOR_PLAYLIST)
            .columns(CREATOR_PLAYLIST.CREATOR_ID, CREATOR_PLAYLIST.PLAYLIST_ID)
            .values(creatorId, playlistId));
  }

  public Flux<Integer> savePlaylistToCreator(
      @NotNull String creatorId, @NotNull List<String> playlistIds) {
    return Flux.from(
        dsl.batch(
            playlistIds.stream()
                .map(
                    m ->
                        dsl.insertInto(CREATOR_PLAYLIST)
                            .columns(CREATOR_PLAYLIST.CREATOR_ID, CREATOR_PLAYLIST.PLAYLIST_ID)
                            .values(creatorId, m))
                .collect(Collectors.toUnmodifiableList())));
  }

  public Mono<Integer> deletePlaylistFromCreator(
      @NotNull String creatorId, @NotNull String playlistId) {
    var eqCreatorID = CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId);
    var eqPlayListID = CREATOR_PLAYLIST.PLAYLIST_ID.eq(playlistId);
    return Mono.from(dsl.deleteFrom(CREATOR_PLAYLIST).where(eqCreatorID).and(eqPlayListID));
  }

  public Mono<Integer> deletePlaylistsFromCreator(
      @NotNull String creatorId, @NotNull List<String> playlistIds) {
    var eqCreateID = CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId);
    var allPlaylistIDs = CREATOR_PLAYLIST.PLAYLIST_ID.in(playlistIds);
    return Mono.from(dsl.deleteFrom(CREATOR_PLAYLIST).where(eqCreateID.and(allPlaylistIDs)));
  }

  public Flux<PlayList> listPlaylistsByCreatorId(String creatorId) {
    var eqCreatorID = CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId);
    var eqPlayListID = CREATOR_PLAYLIST.PLAYLIST_ID.eq(PLAYLIST.ID);
    var leftJoin = CREATOR_PLAYLIST.leftJoin(PLAYLIST);
    return Flux.from(
            dsl.select(PLAYLIST.fields())
                .from(leftJoin.on(eqPlayListID))
                .where(eqCreatorID.and(DELETED_FLAG))
                .orderBy(PLAYLIST.CREATE_TIME.desc()))
        .map(r -> r.into(RPlaylist.class))
        .switchIfEmpty(
            Flux.error(
                new IllegalArgumentException("playlist not found with createrId: " + creatorId)))
        .map(PlayListMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }
}
