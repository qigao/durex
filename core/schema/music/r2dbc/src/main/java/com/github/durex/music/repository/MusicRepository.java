package com.github.durex.music.repository;

import static com.github.durex.model.tables.QMusic.MUSIC;

import com.github.durex.music.mapper.MusicMapper;
import com.github.durex.music.model.Music;
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
public class MusicRepository {
  public static final Condition NOT_DELETED = MUSIC.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  /**
   * @param title title of music
   * @return list of found rows
   */
  public Flux<Music> findByTitle(@NotNull String title) {
    return Flux.from(dsl.selectFrom(MUSIC).where(MUSIC.TITLE.eq(title)).and(NOT_DELETED))
        .switchIfEmpty(
            Flux.error(new IllegalArgumentException("no musics found with title: " + title)))
        .map(MusicMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  /**
   * find musics by title, must be not null, and not empty or blank.
   *
   * @param title title of music must be not null, and not empty or blank. wildcard is %title,
   *     %title%, title%
   * @param wildCardType wildcard type
   * @return list of found rows
   */
  public Flux<Music> findByTitle(@NotNull String title, WildCardType wildCardType) {
    String clauseBuilder = SqlHelper.likeClauseBuilder(wildCardType, title);
    var like = MUSIC.TITLE.like(clauseBuilder);
    return Flux.from(dsl.selectFrom(MUSIC).where(like).and(NOT_DELETED))
        .switchIfEmpty(
            Flux.error(new IllegalArgumentException("no musics found with title: " + title)))
        .map(MusicMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  /**
   * find music by id.
   *
   * @param id id of music
   * @return music
   */
  public Mono<Music> findById(@NotNull String id) {
    return Mono.from(dsl.selectFrom(MUSIC).where(MUSIC.ID.eq(id)).and(NOT_DELETED))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("no music found with id: " + id)))
        .map(MusicMapper::mapRecordToDto);
  }

  /**
   * list of musics which are not deleted.
   *
   * @return List<{@link Music}>
   */
  public Flux<Music> findAllAvailable() {
    return Flux.from(dsl.selectFrom(MUSIC).where(NOT_DELETED))
        .switchIfEmpty(Flux.error(new IllegalArgumentException("no musics found")))
        .map(MusicMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  public Flux<Music> findAllDeleted() {
    return Flux.from(dsl.selectFrom(MUSIC).where(MUSIC.DELETED_FLAG.eq(1)))
        .switchIfEmpty(Flux.error(new IllegalArgumentException("no musics found")))
        .map(MusicMapper::mapRecordToDto)
        .publish()
        .autoConnect();
  }

  /**
   * set delete flag to 1.
   *
   * @param id id of music
   * @return number of updated rows
   */
  public Mono<Integer> deleteById(@NotNull String id) {
    return Mono.from(
        dsl.update(MUSIC)
            .set(MUSIC.DELETED_FLAG, 1)
            .set(MUSIC.DELETE_TIME, LocalDateTime.now())
            .where(MUSIC.ID.eq(id)));
  }

  /**
   * set delete flag to 1.
   *
   * @param title title of music
   * @return number of updated rows
   */
  public Mono<Integer> deleteByTitle(@NotNull String title) {
    return Mono.from(
        dsl.update(MUSIC)
            .set(MUSIC.DELETED_FLAG, 1)
            .set(MUSIC.DELETE_TIME, LocalDateTime.now())
            .where(MUSIC.TITLE.eq(title)));
  }

  /**
   * @param title title of music
   * @return number of inserted rows
   */
  public Mono<Integer> deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    var likeTitle = MUSIC.TITLE.like(realTitle);
    return Mono.from(
            dsl.update(MUSIC)
                .set(MUSIC.DELETED_FLAG, 1)
                .set(MUSIC.DELETE_TIME, LocalDateTime.now())
                .where(likeTitle)
                .and(NOT_DELETED))
        .doFinally(signalType -> log.info("deleteByTitle"));
  }

  public Mono<Integer> delete(@NotNull List<String> musicIds) {
    return Mono.from(
            dsl.update(MUSIC)
                .set(MUSIC.DELETED_FLAG, 1)
                .set(MUSIC.DELETE_TIME, LocalDateTime.now())
                .where(MUSIC.ID.in(musicIds)))
        .doFinally(signalType -> log.info("delete music"));
  }

  /**
   * save record by insert.
   *
   * @param music music
   * @return insert result
   */
  public Mono<Integer> save(Music music) {
    var rMusic = MusicMapper.mapDtoToRecord(music);
    rMusic.setCreateTime(LocalDateTime.now());
    return Mono.from(dsl.insertInto(MUSIC).set(rMusic));
  }

  /**
   * update record by update.
   *
   * @param musics music list
   * @return int[] updated result of musics
   */
  public Flux<Integer> save(List<Music> musics) {
    return Flux.from(
        dsl.batch(
            musics.stream()
                .map(
                    m -> {
                      var rMusic = MusicMapper.mapDtoToRecord(m);
                      rMusic.setCreateTime(LocalDateTime.now());
                      return dsl.insertInto(MUSIC).set(rMusic).onDuplicateKeyIgnore();
                    })
                .collect(Collectors.toUnmodifiableList())));
  }

  /**
   * update a record.
   *
   * @param music musicDTO ToUpdate
   * @return number of updated records
   */
  public Mono<Integer> update(Music music) {
    var rMusic = MusicMapper.mapDtoToRecord(music);
    rMusic.setUpdateTime(LocalDateTime.now());
    return Mono.from(dsl.update(MUSIC).set(rMusic).where(MUSIC.ID.eq(music.getId())));
  }

  /**
   * update a record
   *
   * @param musics musicDTO ToUpdate
   * @return number of updated records
   */
  public Flux<Integer> update(List<Music> musics) {
    return Flux.from(
        dsl.batch(
            musics.stream()
                .map(
                    m -> {
                      var rMusic = MusicMapper.mapDtoToRecord(m);
                      rMusic.setUpdateTime(LocalDateTime.now());
                      var eqMusicID = MUSIC.ID.eq(m.getId());
                      return dsl.update(MUSIC).set(rMusic).where(eqMusicID);
                    })
                .collect(Collectors.toUnmodifiableList())));
  }
}
