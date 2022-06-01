package com.github.durex.music.repository;

import static com.github.durex.model.tables.QCreatorPlaylist.CREATOR_PLAYLIST;
import static com.github.durex.model.tables.QPlaylist.PLAYLIST;

import com.github.durex.model.tables.records.RCreatorPlaylist;
import com.github.durex.model.tables.records.RPlaylist;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.mapper.PlayListMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
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
    return dsl.deleteFrom(CREATOR_PLAYLIST)
        .where(CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId))
        .and(CREATOR_PLAYLIST.PLAYLIST_ID.eq(playlistId))
        .execute();
  }

  public int[] deletePlaylistsFromCreator(
      @NotNull String creatorId, @NotNull List<String> playlistIds) {
    var records =
        playlistIds.stream()
            .map(
                playlistId ->
                    new RCreatorPlaylist().setCreatorId(creatorId).setPlaylistId(playlistId))
            .collect(Collectors.toList());
    return dsl.batchDelete(records).execute();
  }

  public List<PlayList> listPlaylistsByCreatorId(String creatorId) {
    return dsl
        .select(PLAYLIST.fields())
        .from(CREATOR_PLAYLIST.leftJoin(PLAYLIST).on(CREATOR_PLAYLIST.PLAYLIST_ID.eq(PLAYLIST.ID)))
        .where(CREATOR_PLAYLIST.CREATOR_ID.eq(creatorId).and(DELETED_FLAG))
        .orderBy(PLAYLIST.CREATE_TIME.desc())
        .fetchInto(RPlaylist.class)
        .stream()
        .map(PlayListMapper::mapRecordToDto)
        .collect(Collectors.toList());
  }
}
