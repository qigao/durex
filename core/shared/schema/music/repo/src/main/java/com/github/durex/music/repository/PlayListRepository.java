package com.github.durex.music.repository;

import static com.github.durex.api.Tables.PLAYLIST;

import com.github.durex.music.api.PlayList;
import com.github.durex.music.mapper.PlayListMapper;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;

@Slf4j
@ApplicationScoped
public class PlayListRepository {
  public static final Condition NOT_DELETED = PLAYLIST.DELETED_FLAG.eq(0);
  public static final String ERROR_DELETING_PLAY_LIST = "Error deleting PlayList";
  @Inject DSLContext dsl;

  public List<PlayList> findByTitle(@NotNull String title) {
    return dsl.selectFrom(PLAYLIST)
        .where(PLAYLIST.TITLE.eq(title))
        .and(NOT_DELETED)
        .fetch()
        .map(PlayListMapper::mapRecordToDto);
  }

  public List<PlayList> findByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    return dsl.selectFrom(PLAYLIST)
        .where(PLAYLIST.TITLE.like(realTitle))
        .and(NOT_DELETED)
        .fetch()
        .map(PlayListMapper::mapRecordToDto);
  }

  public List<PlayList> findAll() {
    return dsl.selectFrom(PLAYLIST).fetch().map(PlayListMapper::mapRecordToDto);
  }

  public Optional<PlayList> findById(@NotNull String id) {
    var rPlayList = dsl.selectFrom(PLAYLIST).where(PLAYLIST.ID.eq(id)).and(NOT_DELETED).fetchOne();
    return Optional.ofNullable(rPlayList).map(PlayListMapper::mapRecordToDto);
  }

  public int save(@NotNull PlayList playList) {
    try {
      var r = dsl.newRecord(PLAYLIST);
      r.setCreateTime(LocalDateTime.now());
      PlayListMapper.mapDtoToRecord(playList, r);
      return r.insert();
    } catch (Exception e) {
      log.error("save playList error", e);
      return 0;
    }
  }

  public int[] save(List<PlayList> playLists) {
    try {
      var rPlaylists =
          playLists.stream()
              .map(
                  m -> {
                    var rPlaylist = dsl.newRecord(PLAYLIST);
                    rPlaylist.setCreateTime(LocalDateTime.now());
                    PlayListMapper.mapDtoToRecord(m, rPlaylist);
                    return rPlaylist;
                  })
              .collect(Collectors.toList());
      return dsl.batchInsert(rPlaylists).execute();
    } catch (Exception e) {
      log.error("Error saving batch of music", e);
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  }

  public int update(PlayList playList) {
    try {
      var rPlaylist = dsl.newRecord(PLAYLIST);
      PlayListMapper.mapDtoToRecord(playList, rPlaylist);
      rPlaylist.setUpdateTime(LocalDateTime.now());
      return rPlaylist.update();
    } catch (Exception e) {
      log.error("Error updating playList", e);
      return 0;
    }
  }

  public int[] update(List<PlayList> playLists) {
    try {
      var rPlaylists =
          playLists.stream()
              .map(
                  m -> {
                    var rPlaylist = dsl.newRecord(PLAYLIST);
                    rPlaylist.setUpdateTime(LocalDateTime.now());
                    PlayListMapper.mapDtoToRecord(m, rPlaylist);
                    return rPlaylist;
                  })
              .collect(Collectors.toList());
      return dsl.batchUpdate(rPlaylists).execute();
    } catch (Exception e) {
      log.error("Error updating batch of music", e);
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  }

  public int deleteById(@NotNull String id) {
    try (var crud =
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)) {
      return crud.where(PLAYLIST.ID.eq(id)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_PLAY_LIST, e);
      return 0;
    }
  }

  public int deleteById(@NotNull List<String> playlistIds) {
    try (var crud =
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)) {
      return crud.where(PLAYLIST.ID.in(playlistIds)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_PLAY_LIST, e);
      return 0;
    }
  }

  public int deleteByTitle(@NotNull String title) {
    try (var crud =
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)) {
      return crud.where(PLAYLIST.TITLE.eq(title)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_PLAY_LIST, e);
      return 0;
    }
  }

  public int deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    try (var crud =
        dsl.update(PLAYLIST)
            .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
            .set(PLAYLIST.DELETED_FLAG, 1)) {
      return crud.where(PLAYLIST.TITLE.like(realTitle)).execute();
    } catch (Exception e) {
      log.error(ERROR_DELETING_PLAY_LIST, e);
      return 0;
    }
  }
}
