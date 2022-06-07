package com.github.durex.music.controller;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.shared.annotations.Param;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.utils.Helper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

@RequestScoped
@Path("/v1/music")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Tag(name = "Music")
@Slf4j
@Param
// @Logged
public class MusicController {

  @Inject MusicService musicService;
  @Inject RedissonClient redissonClient;

  @GET
  @Path("/")
  @Operation(
      summary = "Query all musics",
      description = "query by title with paging, default page size is 10")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData<List<Music>>> getMusic(
      @Parameter(description = "music title") @QueryParam("title") @Encoded String title,
      @Parameter(description = "music id,used when paging") @QueryParam("id") @Encoded
          String musicId,
      @Parameter(description = "page size") @QueryParam("offset") @Encoded @DefaultValue("10")
          int offset) {
    log.info("getMusic title:{},musicId:{},offset:{}", title, musicId, offset);
    return Multi.createFrom()
        .publisher(musicService.getMusicsByTitle(title))
        .collect()
        .asList()
        .map(data -> RespData.of(data, Helper.okResponse()));
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "get music info", description = " ")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData<Music>> getMusic(
      @Parameter(description = "music id ") @PathParam("id") String musicId) {
    RMapReactive<String, Music> cachedMusic = redissonClient.reactive().getMap("music");
    Mono<Music> mono = musicService.getMusicById(musicId);
    var result = cachedMusic.get(musicId).switchIfEmpty(mono);
     return Uni.createFrom().publisher(result).map(data -> RespData.of(data, Helper.okResponse()));
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "delete a music", description = "admin only")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData<Integer>> deleteMusic(
      @Parameter(description = "music id ") @PathParam("id") String id) {
    return Uni.createFrom()
        .publisher(musicService.deleteMusicById(id))
        .map(p -> RespData.of(p, Helper.okResponse()));
  }

  @POST
  @Path("/")
  @Operation(summary = "save a music info", description = "admin only")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData<Integer>> createMusic(Music musicReq) {
    return Uni.createFrom()
        .publisher(musicService.createMusic(musicReq))
        .map(p -> RespData.of(p, Helper.okResponse()));
  }

  @PUT
  @Path("/")
  @Operation(summary = "update music info", description = "admin only")
  @APIResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RespData.class)))
  public Uni<RespData<Integer>> updateMusic(Music musicReq) {
    log.info("update music: {}", musicReq);
    return Uni.createFrom()
        .publisher(musicService.updateMusic(musicReq))
        .map(p -> RespData.of(p, Helper.okResponse()));
  }
}
