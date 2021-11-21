package com.github.durex.basic.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.basic.exception.MusicNotFoundException;
import com.github.durex.basic.exceptionhandler.ExceptionHandler;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.service.MusicService;
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
@Path("/v1/music")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Tag(name = "Music")
public class MusicController {
  @Inject MusicService musicService;

  @GET
  @Path("")
  @Operation(summary = "获取音乐信息 ", description = "获取音乐，可分页获取， 默认10条")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MusicRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "music file not found",
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
  public List<MusicRequest> getMusic(
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
      throws MusicNotFoundException {
    return EntityMapper.musicRequestListMapper(
        musicService.getMusicsByTitleAndEditor(title, editor, start, end));
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取音乐信息", description = "获取音乐，可分页获取，默认10条")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MusicRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "music file not found",
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
  public MusicRequest getMusic(
      @Parameter(description = "音乐ID ") @PathParam("id") Long id,
      @Parameter(description = "用户ID，用户ID存在时为私有音乐，仅用户自己可见，用户ID为空时，音乐为公共音乐，所有用户均可查询")
          @QueryParam("editor")
          @Encoded
          String editor)
      throws MusicNotFoundException {
    return EntityMapper.musicEntityToRequest(musicService.getMusicByIdAndEditor(id, editor));
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "更新音乐信息", description = "更新音乐信息，用户ID为空时为公共音乐，否则为私有音乐，仅用户自己可见")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MusicRequest.class))),
        @APIResponse(
            responseCode = "404",
            description = "Music not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class))),
        @APIResponse(
            responseCode = "400",
            description = "music file update error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public MusicRequest updateMusic(
      @Parameter(description = "音乐ID ") @PathParam("id") Long id,
      @QueryParam("editor") @Encoded String editor,
      MusicRequest musicRequest)
      throws MusicNotFoundException {
    var music = musicService.updateMusic(id, editor, musicRequest);
    return EntityMapper.musicEntityToRequest(music);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "删除音乐信息", description = "通过音乐ID删除，当音乐不存在与任何一个歌单时方能删除，删除时不删除音乐流文件，用户可删除私有歌曲")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "204", description = "Success"),
        @APIResponse(
            responseCode = "404",
            description = "music not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class))),
        @APIResponse(
            responseCode = "400",
            description = "music file delete error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public Response deleteMusic(
      @PathParam("id") Long id, @QueryParam("editor") @Encoded String editor)
      throws MusicNotFoundException {
    musicService.deleteMusic(id, editor);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @POST
  @Path("/")
  @Operation(summary = "生成音乐信息", description = "生成音乐信息，用户ID为空时，为公共音乐，否则为私有音乐，仅用户自己可见")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MusicRequest.class))),
        @APIResponse(
            responseCode = "400",
            description = "music file create error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionHandler.ErrorResponseBody.class)))
      })
  public MusicRequest createMusic(
      @Parameter(description = "用户ID") @QueryParam("editor") @Encoded String editor,
      MusicRequest musicRequest) {
    var music = musicService.createMusic(editor, musicRequest);
    return EntityMapper.musicEntityToRequest(music);
  }
}
