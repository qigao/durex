package com.github.durex.music.repository;

import static com.github.durex.api.tables.QMusic.MUSIC;

import com.github.durex.music.api.Music;
import com.github.durex.music.mapper.MusicMapper;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;

@Slf4j
@RequestScoped
public class MusicRepository {
  public static final Condition NOT_DELETED = MUSIC.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  /**
   * find musics by titles, title must not be null, empty or blank,if titles is empty, return empty
   * list.
   *
   * @param title title of music
   * @return list of found rows
   */
  public List<Music> findByTitle(@NonNull String title) {
    return dsl.selectFrom(MUSIC)
        .where(MUSIC.TITLE.eq(title))
        .and(NOT_DELETED)
        .fetch()
        .map(MusicMapper::mapRecordToDto);
  }

  /**
   * find musics by title.
   *
   * @param title title of music ,must not be null, empty or blank
   * @param wildCardType wild card type {@link WildCardType}
   * @return list of found rows
   */
  public List<Music> findByTitle(@NonNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    return dsl.selectFrom(MUSIC)
        .where(MUSIC.TITLE.like(realTitle))
        .and(NOT_DELETED)
        .fetch()
        .map(MusicMapper::mapRecordToDto);
  }

  /**
   * find music by id.
   *
   * @param id id of music
   * @return music
   */
  public Optional<Music> findById(@NonNull String id) {
    var rMusic = dsl.selectFrom(MUSIC).where(MUSIC.ID.eq(id)).and(NOT_DELETED).fetchOne();
    return Optional.ofNullable(rMusic).map(MusicMapper::mapRecordToDto);
  }

  /** @return list of musics */
  public List<Music> findAll() {
    return dsl.selectFrom(MUSIC).fetch().stream()
        .map(MusicMapper::mapRecordToDto)
        .collect(Collectors.toList());
  }

  /**
   * set delete flag to 1
   *
   * @param id id of music
   * @return number of updated rows
   */
  public int deleteById(@NonNull String id) {
    return dsl.update(MUSIC)
        .set(MUSIC.DELETED_FLAG, 1)
        .set(MUSIC.DELETE_TIME, LocalDateTime.now())
        .where(MUSIC.ID.eq(id))
        .execute();
  }

  /**
   * This method will not delete a music physically, but set delete flag to 1
   *
   * @param title title of musi
   * @return number of updated rows
   */
  public int deleteByTitle(@NonNull String title) {
    return dsl.update(MUSIC)
        .set(MUSIC.DELETED_FLAG, 1)
        .set(MUSIC.DELETE_TIME, LocalDateTime.now())
        .where(MUSIC.TITLE.eq(title))
        .execute();
  }

  /**
   * @param title title of music
   * @return number of inserted rows
   */
  public int deleteByTitle(@NonNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    return dsl.update(MUSIC)
        .set(MUSIC.DELETED_FLAG, 1)
        .set(MUSIC.DELETE_TIME, LocalDateTime.now())
        .where(MUSIC.TITLE.like(realTitle))
        .execute();
  }

  public int delete(@NonNull List<String> musicIds) {
    return dsl.update(MUSIC)
        .set(MUSIC.DELETED_FLAG, 1)
        .set(MUSIC.DELETE_TIME, LocalDateTime.now())
        .where(MUSIC.ID.in(musicIds))
        .execute();
  }

  /**
   * save record by insert
   *
   * @param music music
   * @return insert result
   */
  public int save(Music music) {
    var rMusic = dsl.newRecord(MUSIC);
    rMusic.setCreateTime(LocalDateTime.now());
    MusicMapper.mapDtoToRecord(music, rMusic);
    return rMusic.insert();
  }

  /**
   * update record by update
   *
   * @param musics music list
   * @return int[] updated result of musics
   */
  public int[] save(List<Music> musics) {
    var rMusics =
        musics.stream()
            .map(
                m -> {
                  var rMusic = dsl.newRecord(MUSIC);
                  MusicMapper.mapDtoToRecord(m, rMusic);
                  rMusic.setCreateTime(LocalDateTime.now());
                  return rMusic;
                })
            .collect(Collectors.toList());
    return dsl.batchInsert(rMusics).execute();
  }

  /**
   * update a record
   *
   * @param music musicDTO ToUpdate
   * @return number of updated records
   */
  public int update(Music music) {
    var rMusic = dsl.newRecord(MUSIC);
    MusicMapper.mapDtoToRecord(music, rMusic);
    rMusic.setUpdateTime(LocalDateTime.now());
    return rMusic.update();
  }

  /**
   * batch update
   *
   * @param musics musicDTOs ToUpdate
   * @return number of updated records
   */
  public int[] update(List<Music> musics) {
    var rMusics =
        musics.stream()
            .map(
                m -> {
                  var rMusic = dsl.newRecord(MUSIC);
                  rMusic.setUpdateTime(LocalDateTime.now());
                  MusicMapper.mapDtoToRecord(m, rMusic);
                  return rMusic;
                })
            .collect(Collectors.toList());
    return dsl.batchUpdate(rMusics).execute();
  }
}
