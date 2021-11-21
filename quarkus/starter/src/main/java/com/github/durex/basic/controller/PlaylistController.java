package com.github.durex.basic.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.basic.exception.PlayListNotFoundException;
import com.github.durex.basic.exceptionhandler.ExceptionHandler;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.basic.service.PlaylistService;
import com.github.durex.basic.util.EntityMapper;
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
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@RequestScoped
@Path("/v1/playlist")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Tag(name = "Playlist")
public class PlaylistController {
  @Inject PlaylistService playlistService;

  @GET
  @Path("/")
  @Operation(summary = "获取所有歌单信息")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlayListRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "Playlist not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class))),
        @APIResponse(
            responseCode = "400",
            description = "music file find error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public List<PlayListRequest> getPlaylist(
      @Parameter(description = "用户ID，用户ID存在时为私有音乐，仅用户自己可见，用户ID为空时，音乐为公共音乐，所有用户均可查询")
          @QueryParam("editor")
          @Encoded
          String editor,
      @Parameter(description = "音乐名，支持中文模糊查询") @QueryParam("title") @Encoded String title,
      @DefaultValue("0")
          @Parameter(description = "start of query range")
          @QueryParam("start")
          @Encoded
          int start,
      @DefaultValue("10") @Parameter(description = "end of query range") @QueryParam("end") @Encoded
          int end)
      throws PlayListNotFoundException {
    var playlist = playlistService.getPlayListByEditorAndTitle(title, editor, start, end);
    return EntityMapper.playListToRequest(playlist);
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取歌单信息", description = "获取歌单信息，分页获取，默认10条")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlayListRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "Playlist not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public PlayListRequest getPlaylist(@PathParam("id") Long id) throws PlayListNotFoundException {
    var playlist = playlistService.getPlaylist(id);
    return EntityMapper.playListToRequest(playlist);
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "更新歌单信息", description = "更新歌单信息，音乐必须为存在,用户不可更改公共歌单")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlayListRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "Playlist not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public PlayListRequest updatePlaylist(
      @PathParam("id") Long id,
      @QueryParam("editor") @Encoded String editor,
      PlayListRequest playList)
      throws PlayListNotFoundException {
    var m3u8s = playlistService.updatePlaylist(id, editor, playList);
    return EntityMapper.playListToRequest(m3u8s);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      summary = "删除歌单信息",
      description = "删除歌单信息，不删除音乐，当音乐不存在与任何一个歌单时方能删除，删除时不删除音乐流文件，用户ID可删除私有歌单")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "204", description = "Success"),
        @APIResponse(
            responseCode = "404",
            description = "PlayList not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public Response deletePlaylist(@PathParam("id") Long id) {
    playlistService.deletePlaylist(id);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @POST
  @Path("/")
  @Operation(summary = "生成歌单信息", description = "生成歌单信息时，音乐必须是已经存在的，用户ID为空时，为私有歌单，仅用户可见，否则为公共歌单")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlayListRequest.class))),
        @APIResponse(
            responseCode = "400",
            description = "Error creating a new playlist",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public PlayListRequest createPlaylist(
      @QueryParam("editor") @Encoded String editor, PlayListRequest playList) {
    var m3u8s = playlistService.createPlaylist(editor, playList);
    return EntityMapper.playListToRequest(m3u8s);
  }
}
