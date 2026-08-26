package com.github.durex.music.repository;

import static com.github.durex.model.tables.QPlaylist.PLAYLIST;

import com.github.durex.music.mapper.PlayListMapper;
import com.github.durex.music.model.PlayList;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;

@Slf4j
public class PlayListRepository {
  public static final Condition NOT_DELETED = PLAYLIST.DELETED_FLAG.eq(0);
  private final DSLContext dsl;

  public PlayListRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

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
    return dsl.selectFrom(PLAYLIST).where(NOT_DELETED).fetch().map(PlayListMapper::mapRecordToDto);
  }

  public Optional<PlayList> findById(@NotNull String id) {
    var rPlayList = dsl.selectFrom(PLAYLIST).where(PLAYLIST.ID.eq(id)).and(NOT_DELETED).fetchOne();
    return Optional.ofNullable(rPlayList).map(PlayListMapper::mapRecordToDto);
  }

  public int save(@NotNull PlayList playList) {
    var r = dsl.newRecord(PLAYLIST);
    r.setCreateTime(LocalDateTime.now());
    PlayListMapper.mapDtoToRecord(playList, r);
    return r.insert();
  }

  public int[] save(List<PlayList> playLists) {
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
  }

  public int update(PlayList playList) {
    return updateQuery(playList).execute();
  }

  public int[] update(List<PlayList> playLists) {
    return dsl.batch(playLists.stream().map(this::updateQuery).toList()).execute();
  }

  public int deleteById(@NotNull String id) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.ID.eq(id))
        .and(NOT_DELETED)
        .execute();
  }

  public int deleteById(@NotNull List<String> playlistIds) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.ID.in(playlistIds))
        .and(NOT_DELETED)
        .execute();
  }

  public int deleteByTitle(@NotNull String title) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.TITLE.eq(title))
        .and(NOT_DELETED)
        .execute();
  }

  public int deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.TITLE.like(realTitle))
        .and(NOT_DELETED)
        .execute();
  }

  private Query updateQuery(PlayList playList) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DESCRIPTION, playList.getDescription())
        .set(PLAYLIST.TITLE, playList.getTitle())
        .set(PLAYLIST.COVER_ID, playList.getCoverId())
        .set(PLAYLIST.UPDATE_TIME, LocalDateTime.now())
        .where(PLAYLIST.ID.eq(playList.getId()))
        .and(NOT_DELETED);
  }
}
