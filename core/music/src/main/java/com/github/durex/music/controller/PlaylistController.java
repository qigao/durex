package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.music.annotations.Auth;
import com.github.durex.music.annotations.Logged;
import com.github.durex.music.model.MusicIDListRequest;
import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.model.PlayListRequest;
import com.github.durex.music.model.RespData;
import com.github.durex.music.service.PlayListMusicService;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.music.util.EntityMapper;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/v1/playlist")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Tag(name = "Playlist")
@Slf4j
@Logged
@Auth
public class PlaylistController {
  private static final String SUCCESS = "success";

  @Inject PlaylistService playlistService;
  @Inject PlayListMusicService playListMusicService;

  @GET
  @Path("/")
  @Operation(summary = "获取所有歌单信息", description = "获取音乐列表，可分页获取，data为歌单列表List<PlayListRequest>")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> getPlaylist(
      @Parameter(description = "用户ID，用户ID存在时为私有音乐，仅用户自己可见，用户ID为空时，音乐为公共音乐，所有用户均可查询")
          @QueryParam("editor")
          @Encoded
          String editor,
      @Parameter(description = "DeviceID ") @QueryParam("deviceId") @Encoded String deviceId,
      @Parameter(description = "音乐名，支持中文模糊查询") @QueryParam("title") @Encoded String title,
      @DefaultValue("0")
          @Parameter(description = "start of query range")
          @QueryParam("start")
          @Encoded
          int start,
      @DefaultValue("10") @Parameter(description = "end of query range") @QueryParam("end") @Encoded
          int end) {
    log.info("deviceId: {},editor: {}", deviceId, editor);
    var playlist = playlistService.findPlayListByEditorAndDevice(editor, deviceId, start, end);
    var playListToResp = EntityMapper.playListToResp(playlist);
    return RespData.builder().result(200).msg(SUCCESS).data(playListToResp).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取歌单信息", description = "获取歌单信息，data为歌单对象PlayListRequest")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> getPlaylist(@PathParam("id") String id) {
    var playlist = playlistService.findPlayListById(id);
    var playlistResp = EntityMapper.playListToResp(playlist);
    return RespData.builder().result(200).msg(SUCCESS).data(playlistResp).build();
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "更新歌单信息", description = "更新歌单信息，音乐必须为存在,用户不可更改公共歌单，data为歌单PlayListRequest")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> updatePlaylist(
      @PathParam("id") String id,
      @QueryParam("editor") @Encoded String editor,
      PlayListRequest playList) {
    playlistService.updatePlaylist(id, editor, playList);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      summary = "删除歌单信息",
      description = "删除歌单信息，不删除音乐，当音乐不存在与任何一个歌单时方能删除，删除时不删除音乐流文件，用户ID可删除私有歌单,data为空")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> deletePlaylist(@PathParam("id") String id) {
    var affectedRows = playlistService.deletePlaylist(id);
    log.info("delete playlist affectedRows: {}", affectedRows);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @POST
  @Path("/")
  @Operation(
      summary = "生成歌单信息",
      description =
          "生成歌单信息时，音乐必须是已经存在的，用户ID为空时，为私有歌单，仅用户可见，否则为公共歌单,data为歌单PlayListID: 01FP03RRF39NDVCND81G72NG34")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> createPlaylistWithID(
      @QueryParam("editor") @Encoded String editor, PlayListRequest playList) {
    var id = playlistService.createPlaylist(editor, playList);
    log.info("created playlist ID: {}", id);
    return RespData.builder().result(200).msg(SUCCESS).data(id).build();
  }

  @PUT
  @Path("update/{id}")
  @Operation(summary = "从歌单批量增加歌曲", description = "更新歌单信息，音乐ID必须为存在,用户不可更改公共歌单，data为空")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> updateMusicToPlayList(
      @PathParam("id") String id,
      @QueryParam("editor") @Encoded String editor,
      MusicIDListRequest musicIDRequest) {
    var isAdded =
        playListMusicService.checkMusicIdThenAddToPlayList(id, musicIDRequest.getMusics());
    log.info(
        "music ids {} were added to playlist: {}, result: {}",
        musicIDRequest.getMusics(),
        id,
        isAdded);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @DELETE
  @Path("update/{id}")
  @Operation(summary = "从歌单批量删除歌曲", description = "更新歌单信息，音乐ID必须为存在,用户不可更改公共歌单，data为空")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> deleteMusicsFromPlayList(
      @PathParam("id") String id,
      @QueryParam("editor") @Encoded String editor,
      MusicIDListRequest musicIDList) {
    var isDeleted =
        playListMusicService.checkMusicIdThenDeleteFromPlayList(id, musicIDList.getMusics());
    log.info("music id {} were deleted from playlist: {}, result: {}", musicIDList, id, isDeleted);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @PUT
  @Path("order/{id}")
  @Operation(summary = "从歌单批量增加歌曲", description = "更新歌单信息，音乐ID必须为存在,用户不可更改公共歌单，data为空")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> updateMusicWithOrderToPlayList(
      @PathParam("id") String id,
      @QueryParam("editor") @Encoded String editor,
      List<OrderedMusicRequest> musicIDRequest) {
    var isAdded = playListMusicService.checkMusicIdThenAddToPlayListWithOrder(id, musicIDRequest);
    log.info("music ids {} were added to playlist: {}, result: {}", musicIDRequest, id, isAdded);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }
}
