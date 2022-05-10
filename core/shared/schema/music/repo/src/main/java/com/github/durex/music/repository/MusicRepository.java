package com.github.durex.music.repository;

import static com.github.durex.api.tables.TMusic.MUSIC;

import com.github.durex.music.api.Music;
import com.github.durex.music.mapper.MusicMapper;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;

@Slf4j
@RequestScoped
public class MusicRepository {
  public static final String ERROR_DELETING_MUSIC = "Error deleting music";
  public static final Condition NOT_DELETED = MUSIC.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  /**
   * @param title title of music
   * @return list of found rows
   */
  public List<Music> findByTitle(@NotNull String title) {
    try (var crud = dsl.selectFrom(MUSIC)) {
      return crud.where(MUSIC.TITLE.eq(title))
          .and(NOT_DELETED)
          .fetch()
          .map(MusicMapper::mapRecordToDto);
    } catch (Exception e) {
      log.error("Error fetching musics", e);
      return Collections.emptyList();
    }
  }

  /**
   * @param title title of music
   * @return list of found rows
   */
  public List<Music> findByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    try (var crud = dsl.selectFrom(MUSIC)) {
      return crud.where(MUSIC.TITLE.like(realTitle))
          .and(NOT_DELETED)
          .fetch()
          .map(MusicMapper::mapRecordToDto);
    } catch (Exception e) {
      log.error("Error fetching musics", e);
      return Collections.emptyList();
    }
  }

  /**
   * @param id id of music
   * @return music
   */
  public Optional<Music> findById(@NotNull String id) {
    try (var crud = dsl.selectFrom(MUSIC)) {
      var rMusic = crud.where(MUSIC.ID.eq(id)).and(NOT_DELETED).fetchOne();
      return Optional.ofNullable(rMusic).map(MusicMapper::mapRecordToDto);
    } catch (Exception e) {
      log.error("Error fetching music", e);
      return Optional.empty();
    }
  }

  /**
   * @return list of musics
   */
  public List<Music> findAll() {
    try (var crud = dsl.selectFrom(MUSIC)) {
      return crud.fetch().stream().map(MusicMapper::mapRecordToDto).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error fetching musics", e);
      return Collections.emptyList();
    }
  }

  /**
   * set delete flag to 1
   *
   * @param id id of music
   * @return number of updated rows
   */
  public int deleteById(@NotNull String id) {
    try (var crud =
        dsl.update(MUSIC).set(MUSIC.DELETED_FLAG, 1).set(MUSIC.DELETE_TIME, LocalDateTime.now())) {
      return crud.where(MUSIC.ID.eq(id)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_MUSIC, e);
      return 0;
    }
  }

  /**
   * set delete flag to 1
   *
   * @param title title of musi
   * @return number of updated rows
   */
  public int deleteByTitle(@NotNull String title) {
    try (var crud =
        dsl.update(MUSIC).set(MUSIC.DELETED_FLAG, 1).set(MUSIC.DELETE_TIME, LocalDateTime.now())) {
      return crud.where(MUSIC.TITLE.eq(title)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_MUSIC, e);
      return 0;
    }
  }

  /**
   * @param title title of music
   * @return number of inserted rows
   */
  public int deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    try (var crud =
        dsl.update(MUSIC).set(MUSIC.DELETED_FLAG, 1).set(MUSIC.DELETE_TIME, LocalDateTime.now())) {
      return crud.where(MUSIC.TITLE.like(realTitle)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_MUSIC, e);
      return 0;
    }
  }

  public int delete(@NotNull List<String> musicIds) {
    try (var crud =
        dsl.update(MUSIC).set(MUSIC.DELETED_FLAG, 1).set(MUSIC.DELETE_TIME, LocalDateTime.now())) {
      return crud.where(MUSIC.ID.in(musicIds)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_MUSIC, e);
      return 0;
    }
  }

  /**
   * save record by insert
   *
   * @param music music
   * @return insert result
   */
  public int save(Music music) {
    try {
      var rMusic = dsl.newRecord(MUSIC);
      rMusic.setCreateTime(LocalDateTime.now());
      MusicMapper.mapDtoToRecord(music, rMusic);
      return rMusic.insert();
    } catch (Exception e) {
      log.error("Error saving music", e);
      return 0;
    }
  }

  /**
   * update record by update
   *
   * @param musics music list
   * @return int[] updated result of musics
   */
  public int[] save(List<Music> musics) {
    try {
      var rMusics =
          musics.stream()
              .map(
                  m -> {
                    var rMusic = dsl.newRecord(MUSIC);
                    MusicMapper.mapDtoToRecord(m, rMusic);
                    return rMusic;
                  })
              .collect(Collectors.toList());
      return dsl.batchInsert(rMusics).execute();
    } catch (Exception e) {
      log.error("Error saving batch of music", e);
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  }

  /**
   * update a record
   *
   * @param music musicDTO ToUpdate
   * @return number of updated records
   */
  public int update(Music music) {
    try {
      var rMusic = dsl.newRecord(MUSIC);
      MusicMapper.mapDtoToRecord(music, rMusic);
      rMusic.setUpdateTime(LocalDateTime.now());
      return rMusic.update();
    } catch (Exception e) {
      log.error("Error updating music", e);
      return 0;
    }
  }

  /**
   * batch update
   *
   * @param musics musicDTOs ToUpdate
   * @return number of updated records
   */
  public int[] update(List<Music> musics) {
    try {
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
    } catch (Exception e) {
      log.error("Error updating batch of music", e);
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  }
}
