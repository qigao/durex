package com.github.durex.music.controller;

import static com.github.durex.support.RespConstant.APPLICATION_JSON;
import static com.github.durex.support.RespConstant.NOTHING_FAILED;
import static com.github.durex.support.RespConstant.OK;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.durex.music.api.PlayList;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.utils.Json;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@QuarkusTest
@TestHTTPEndpoint(PlaylistController.class)
class PlaylistControllerTest {

  @InjectMock PlaylistService playlistService;

  @Test
  void testGetPlaylistByTitle() {
    var playlistResp = DemoMusicData.givenSomePlayList(5);
    when(playlistService.findPlayListByTitle(anyString()))
        .thenReturn(Flux.fromIterable(playlistResp));
    given()
        .when()
        .queryParam("title", "test")
        .get("/")
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", hasSize(5));
  }

  @Test
  void testGetPlayListById() {
    var playlistResp = DemoMusicData.givenAPlayList();
    when(playlistService.findPlayListById(anyString())).thenReturn(Mono.just(playlistResp));
    given()
        .when()
        .pathParam("id", "test")
        .get("/{id}")
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result.id", equalTo(playlistResp.getId()));
  }

  @Test
  void testSavePlayList() throws IOException {
    var playlistResp = DemoMusicData.givenAPlayListMusic();
    when(playlistService.createPlaylist(any())).thenReturn(Flux.just(1, 1, 1));
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(playlistResp))
        .post()
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", hasSize(3));
  }

  @Test
  void testUpdatePlayList() throws IOException {
    var playlist = DemoMusicData.givenAPlayList();
    when(playlistService.updatePlaylist(any(PlayList.class))).thenReturn(Mono.just(1));
    given()
        .when()
        .pathParam("id", "test")
        .contentType(APPLICATION_JSON)
        .body(Json.toString(playlist))
        .put("/{id}")
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", equalTo(1));
  }

  @Test
  void testDeletePlayList() {
    when(playlistService.deletePlaylistById(anyString())).thenReturn(Mono.just(1));
    given()
        .when()
        .pathParam("id", "test")
        .delete("/{id}")
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", equalTo(1));
  }
}
