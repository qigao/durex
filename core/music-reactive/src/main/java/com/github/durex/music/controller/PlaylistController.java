package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.music.api.PlayList;
import com.github.durex.music.api.PlayListMusic;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.shared.api.RespData;
import com.github.durex.shared.utils.Helper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
  public Multi<RespData> getPlaylistByTitle(
      @Parameter(description = "music title") @QueryParam("title") @Encoded String title,
      @DefaultValue("10") @Parameter(description = "query page size") @QueryParam("offset") @Encoded
          int offset) {
    return Multi.createFrom()
        .publisher(playlistService.findPlayListByTitle(title))
        .map(p -> RespData.builder().error(Helper.okResponse()).data(p).build());
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "获取歌单信息", description = "获取歌单信息，data为歌单对象PlayListRequest")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData> getPlaylist(@PathParam("id") String id) {
    return Uni.createFrom()
        .publisher(playlistService.findPlayListById(id))
        .map(p -> RespData.builder().error(Helper.okResponse()).data(p).build());
  }

  @POST
  @Path("/")
  @Operation(summary = "create a playlist with music ", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Multi<RespData> createPlaylist(PlayListMusic playListMusic) {
    return Multi.createFrom()
        .publisher(playlistService.createPlaylist(playListMusic))
        .map(p -> RespData.builder().error(Helper.okResponse()).data(p).build());
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "update a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData> updatePlaylist(PlayList playList) {
    return Uni.createFrom()
        .publisher(playlistService.updatePlaylist(playList))
        .map(p -> RespData.builder().error(Helper.okResponse()).data(p).build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "delete a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData> deletePlaylist(@PathParam("id") String id) {
    return Uni.createFrom()
        .publisher(playlistService.deletePlaylistById(id))
        .map(p -> RespData.builder().error(Helper.okResponse()).data(p).build());
  }
}
