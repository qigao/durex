package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.music.annotations.Logged;
import com.github.durex.music.annotations.Param;
import com.github.durex.music.model.MusicRequest;
import com.github.durex.music.model.MusicResp;
import com.github.durex.music.model.RespData;
import com.github.durex.music.service.MusicService;
import com.github.durex.music.util.EntityMapper;
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
@Path("/v1/music")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Tag(name = "Music")
@Slf4j
@Param
@Logged
public class MusicController {
  private static final String SUCCESS = "success";
  @Inject MusicService musicService;

  @GET
  @Path("/")
  @Operation(summary = "查询音乐信息 ", description = "获取音乐，可分页获取， 默认10条,data为音乐列表List<MusicRequest>")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> getMusic(
      @Parameter(description = "用户ID，用户ID存在时为私有音乐，仅用户自己可见，用户ID为空时，音乐为公共音乐，所有用户均可查询")
          @QueryParam("editor")
          @Encoded
          String editor,
      @Parameter(description = "DeviceID") @QueryParam("deviceId") @Encoded String deviceId,
      @Parameter(description = "音乐名，支持中文模糊查询") @QueryParam("title") @Encoded String title,
      @DefaultValue("0")
          @Parameter(description = "start of query range")
          @QueryParam("start")
          @Encoded
          int start,
      @DefaultValue("10") @Parameter(description = "end of query range") @QueryParam("end") @Encoded
          int end) {
    java.util.List<MusicResp> musicList;
    if (deviceId == null) {
      musicList =
          EntityMapper.musicListToMusicRespMapper(
              musicService.getMusicsByTitleAndEditor(title, editor, start, end));
    } else {
      musicList =
          EntityMapper.musicListToMusicRespMapper(
              musicService.getMusicsByEditorAndDevice(editor, deviceId, start, end));
    }
    return RespData.builder().result(200).msg(SUCCESS).data(musicList).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取音乐信息", description = "获取音乐,data为音乐对象MusicResp,Order为0")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> getMusic(
      @Parameter(description = "音乐ID ") @PathParam("id") String id,
      @Parameter(description = "用户ID，用户ID存在时为私有音乐，仅用户自己可见，用户ID为空时，音乐为公共音乐，所有用户均可查询")
          @QueryParam("editor")
          @Encoded
          String editor) {
    var musicResp =
        EntityMapper.musicEntityToResp(musicService.getMusicByIdAndEditor(id, editor), 0);
    return RespData.builder().result(200).msg(SUCCESS).data(musicResp).build();
  }

  @PUT
  @Path("/{id}")
  @Operation(
      summary = "更新音乐信息",
      description = "更新音乐信息，用户ID为空时为公共音乐，否则为私有音乐，仅用户自己可见, data为更改后的音乐对象MusicRequest")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> updateMusic(
      @Parameter(description = "音乐ID ") @PathParam("id") String id,
      @QueryParam("editor") @Encoded String editor,
      MusicRequest musicRequest) {
    log.info("update music: {}", musicRequest);
    musicService.updateMusic(id, editor, musicRequest);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      summary = "删除音乐信息",
      description = "通过音乐ID删除，当音乐不存在与任何一个歌单时方能删除，删除时不删除音乐流文件，用户可删除私有歌曲，成功返回200，data为空")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> deleteMusic(
      @PathParam("id") String id, @QueryParam("editor") @Encoded String editor) {
    musicService.deleteMusic(id, editor);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }

  @POST
  @Path("/")
  @Operation(
      summary = "生成音乐信息",
      description = "生成音乐信息，用户ID为空时，为公共音乐，否则为私有音乐，仅用户自己可见,成功返回200，data为音乐信息")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Object> createMusic(
      @Parameter(description = "用户ID") @QueryParam("editor") @Encoded String editor,
      MusicRequest musicRequest) {
    musicService.createMusic(editor, musicRequest);
    return RespData.builder().result(200).msg(SUCCESS).build();
  }
}
