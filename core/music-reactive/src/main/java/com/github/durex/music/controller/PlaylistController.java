package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.music.api.PlayList;
import com.github.durex.music.api.PlayListMusic;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.shared.api.RespData;
import com.github.durex.shared.utils.Helper;
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
public class PlaylistController {

  @Inject PlaylistService playlistService;

  @GET
  @Path("/")
  @Operation(summary = "get playlist info", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData getPlaylistByTitle(
      @Parameter(description = "music title") @QueryParam("title") @Encoded String title,
      @DefaultValue("10") @Parameter(description = "query page size") @QueryParam("offset") @Encoded
          int offset) {
    var playlist = playlistService.findPlayListByTitle(title);
    return RespData.builder().error(Helper.okResponse()).data(playlist).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取歌单信息", description = "获取歌单信息，data为歌单对象PlayListRequest")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData getPlaylist(@PathParam("id") String id) {
    var playlist = playlistService.findPlayListById(id);
    return RespData.builder().error(Helper.okResponse()).data(List.of(playlist)).build();
  }

  @POST
  @Path("/")
  @Operation(summary = "create a playlist with music ", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData createPlaylistWithID(PlayListMusic playList) {
    var id = playlistService.createPlaylist(playList);
    log.info("created playlist ID: {}", id);
    return RespData.builder().error(Helper.okResponse()).data(id).build();
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "update a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData updatePlaylist(PlayList playList) {
    var result = playlistService.updatePlaylist(playList);
    return RespData.builder().error(Helper.okResponse()).data(result).build();
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "delete a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData deletePlaylist(@PathParam("id") String id) {
    var affectedRows = playlistService.deletePlaylistById(id);
    log.info("delete playlist affectedRows: {}", affectedRows);
    return RespData.builder().error(Helper.okResponse()).data(affectedRows).build();
  }
}
