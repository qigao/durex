package com.github.durex.music.repository;

import static com.github.durex.model.Tables.PLAYLIST;

import com.github.durex.music.mapper.PlayListMapper;
import com.github.durex.music.model.PlayList;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.time.LocalDateTime;
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
public class PlayListRepository {
  public static final Condition NOT_DELETED = PLAYLIST.DELETED_FLAG.eq(0);
  public static final String ERROR_DELETING_PLAY_LIST = "Error deleting PlayList";
  @Inject DSLContext dsl;

  public Flux<PlayList> findByTitle(@NotNull String title) {
    var eqTitle = PLAYLIST.TITLE.eq(title);
    return Flux.from(dsl.selectFrom(PLAYLIST).where(eqTitle).and(NOT_DELETED))
        .switchIfEmpty(
            Flux.error(new IllegalArgumentException("PlayList not found with title: " + title)))
        .map(PlayListMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  public Flux<PlayList> findByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    var titleCondition = PLAYLIST.TITLE.like(realTitle);
    return Flux.from(dsl.selectFrom(PLAYLIST).where(titleCondition).and(NOT_DELETED))
        .switchIfEmpty(
            Flux.error(new IllegalArgumentException("PlayList not found with title: " + title)))
        .map(PlayListMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  public Flux<PlayList> findAll() {
    return Flux.from(dsl.selectFrom(PLAYLIST))
        .switchIfEmpty(Flux.error(new IllegalArgumentException("PlayList not found")))
        .map(PlayListMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  public Mono<PlayList> findById(@NotNull String id) {
    var condition = PLAYLIST.ID.eq(id).and(NOT_DELETED);
    return Mono.from(dsl.selectFrom(PLAYLIST).where(condition))
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException("PlayList not found with id: " + id)))
        .map(PlayListMapper::mapRecordToDto);
  }

  public Mono<Integer> save(@NotNull PlayList playList) {
    var rPlayList = PlayListMapper.mapDtoToRecord(playList);
    rPlayList.setCreateTime(LocalDateTime.now());
    PlayListMapper.mapDtoToRecord(playList, rPlayList);
    return Mono.from(dsl.insertInto(PLAYLIST).set(rPlayList));
  }

  public Flux<Integer> save(List<PlayList> playLists) {
    return Flux.from(
            dsl.batch(
                playLists.stream()
                    .map(
                        m -> {
                          var rPlaylist = PlayListMapper.mapDtoToRecord(m);
                          rPlaylist.setCreateTime(LocalDateTime.now());
                          return dsl.insertInto(PLAYLIST).set(rPlaylist).onDuplicateKeyIgnore();
                        })
                    .collect(Collectors.toUnmodifiableList())))
        .doOnError(e -> log.error(ERROR_DELETING_PLAY_LIST, e));
  }

  public Mono<Integer> update(PlayList playList) {
    var rPlaylist = PlayListMapper.mapDtoToRecord(playList);
    rPlaylist.setUpdateTime(LocalDateTime.now());
    var eqPlayListID = PLAYLIST.ID.eq(playList.getId());
    return Mono.from(dsl.update(PLAYLIST).set(rPlaylist).where(eqPlayListID.and(NOT_DELETED)))
        .doOnError(e -> log.error(ERROR_DELETING_PLAY_LIST, e));
  }

  public Flux<Integer> update(List<PlayList> playLists) {
    return Flux.from(
        dsl.batch(
            playLists.stream()
                .map(
                    m -> {
                      var rPlaylist = PlayListMapper.mapDtoToRecord(m);
                      rPlaylist.setUpdateTime(LocalDateTime.now());
                      var eqPlayListID = PLAYLIST.ID.eq(m.getId());
                      return dsl.update(PLAYLIST)
                          .set(rPlaylist)
                          .where(eqPlayListID.and(NOT_DELETED));
                    })
                .collect(Collectors.toUnmodifiableList())));
  }

  public Mono<Integer> deleteById(@NotNull String id) {
    return Mono.from(
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)
            .where(PLAYLIST.ID.eq(id)));
  }

  public Mono<Integer> deleteById(@NotNull List<String> playlistIds) {
    return Mono.from(
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)
            .where(PLAYLIST.ID.in(playlistIds)));
  }

  public Mono<Integer> deleteByTitle(@NotNull String title) {
    var eqTitle = PLAYLIST.TITLE.eq(title);
    return Mono.from(
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)
            .where(eqTitle.and(NOT_DELETED)));
  }

  public Mono<Integer> deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    var likeTitle = PLAYLIST.TITLE.like(realTitle);
    return Mono.from(
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)
            .where(likeTitle.and(NOT_DELETED)));
  }
}
