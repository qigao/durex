package com.github.durex.music.repository;

import static com.github.durex.model.tables.QPlaylist.PLAYLIST;

import com.github.durex.music.mapper.PlayListMapper;
import com.github.durex.music.model.PlayList;
import com.github.durex.sqlbuilder.SqlHelper;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;

@Slf4j
@RequestScoped
public class PlayListRepository {
  public static final Condition NOT_DELETED = PLAYLIST.DELETED_FLAG.eq(0);
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
    var rPlaylist = dsl.newRecord(PLAYLIST);
    PlayListMapper.mapDtoToRecord(playList, rPlaylist);
    rPlaylist.setUpdateTime(LocalDateTime.now());
    return rPlaylist.update();
  }

  public int[] update(List<PlayList> playLists) {
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
  }

  public int deleteById(@NotNull String id) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.ID.eq(id))
        .execute();
  }

  public int deleteById(@NotNull List<String> playlistIds) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.ID.in(playlistIds))
        .execute();
  }

  public int deleteByTitle(@NotNull String title) {
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.TITLE.eq(title))
        .execute();
  }

  public int deleteByTitle(@NotNull String title, WildCardType wildCardType) {
    var realTitle = SqlHelper.likeClauseBuilder(wildCardType, title);
    return dsl.update(PLAYLIST)
        .set(PLAYLIST.DELETE_TIME, LocalDateTime.now())
        .set(PLAYLIST.DELETED_FLAG, 1)
        .where(PLAYLIST.TITLE.like(realTitle))
        .execute();
  }
}
