package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.music.api.PlayList;
import com.github.durex.music.api.PlayListMusic;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.shared.api.RespData;
import com.github.durex.shared.utils.Helper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
  public RespData<List<PlayList>> getPlaylistByTitle(
      @Parameter(description = "music title") @QueryParam("title") @Encoded String title,
      @DefaultValue("10") @Parameter(description = "query page size") @QueryParam("offset") @Encoded
          int offset) {
    return RespData.of(playlistService.findPlayListByTitle(title), Helper.okResponse());
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "get playlist info by id", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<PlayList> getPlaylist(@PathParam("id") String id) {
    return RespData.of(playlistService.findPlayListById(id), Helper.okResponse());
  }

  @POST
  @Path("/")
  @Operation(summary = "create a playlist with music ", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<List<Integer>> createPlaylistWithID(PlayListMusic playList) {
    return RespData.of(
        Arrays.stream(playlistService.createPlaylist(playList))
            .boxed()
            .collect(Collectors.toList()),
        Helper.okResponse());
  }

  @PUT
  @Path("/")
  @Operation(summary = "update a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Integer> updatePlaylist(PlayList playList) {
    return RespData.of(playlistService.updatePlaylist(playList), Helper.okResponse());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "delete a playlist", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public RespData<Integer> deletePlaylist(@PathParam("id") String id) {
    return RespData.of(playlistService.deletePlaylistById(id), Helper.okResponse());
  }
}
