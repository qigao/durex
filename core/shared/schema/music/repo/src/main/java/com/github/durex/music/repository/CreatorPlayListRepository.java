package com.github.durex.music.repository;

import static com.github.durex.api.Tables.CREATOR_PLAYLIST;
import static com.github.durex.api.Tables.PLAYLIST;

import com.github.durex.api.tables.records.RCreatorPlaylist;
import com.github.durex.api.tables.records.RPlaylist;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.mapper.PlayListMapper;
import java.util.Collections;
import java.util.List;
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
public class CreatorPlayListRepository {
  public static final Condition DELETED_FLAG = PLAYLIST.DELETED_FLAG.eq(0);
  @Inject DSLContext dsl;

  public int savePlaylistToCreator(@NotNull String creatorId, @NotNull String playlistId) {
    return dsl.insertInto(CREATOR_PLAYLIST)
        .columns(CREATOR_PLAYLIST.CREATOR_ID, CREATOR_PLAYLIST.PLAYLIST_ID)
        .values(creatorId, playlistId)
        .execute();
  }

  public int[] savePlaylistToCreator(@NotNull String creatorId, @NotNull List<String> playlistIds) {
    var records =
        playlistIds.stream()
            .map(
                playlistId ->
                    dsl.newRecord(CREATOR_PLAYLIST)
                        .setCreatorId(creatorId)
                        .setPlaylistId(playlistId))
            .collect(Collectors.toList());
    return dsl.batchInsert(records).execute();
  }

  public int deletePlaylistFromCreator(@NotNull String creatorId, @NotNull String playlistId) {
    try (var crud = dsl.deleteFrom(CREATOR_PLAYLIST)) {
      return crud.where(CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId))
          .and(CREATOR_PLAYLIST.PLAYLIST_ID.eq(playlistId))
          .execute();
    } catch (Exception e) {
      return 0;
    }
  }

  public int[] deletePlaylistsFromCreator(
      @NotNull String creatorId, @NotNull List<String> playlistIds) {
    try {
      var records =
          playlistIds.stream()
              .map(
                  playlistId ->
                      new RCreatorPlaylist().setCreatorId(creatorId).setPlaylistId(playlistId))
              .collect(Collectors.toList());
      return dsl.batchDelete(records).execute();
    } catch (Exception e) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  }

  public List<PlayList> listPlaylistsByCreatorId(String creatorId) {
    try (var seek =
        dsl.select(PLAYLIST.fields())
            .from(
                CREATOR_PLAYLIST
                    .leftJoin(PLAYLIST)
                    .on(CREATOR_PLAYLIST.PLAYLIST_ID.eq(PLAYLIST.ID)))) {
      return seek
          .where(CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId).and(DELETED_FLAG))
          .orderBy(PLAYLIST.CREATE_TIME.desc())
          .fetchInto(RPlaylist.class)
          .stream()
          .map(PlayListMapper::mapRecordToDto)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("error", e);
      return Collections.emptyList();
    }
  }
}
