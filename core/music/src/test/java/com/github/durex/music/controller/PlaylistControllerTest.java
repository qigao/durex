package com.github.durex.music.controller;

import static com.github.durex.support.RespConstant.NOTHING_FAILED;
import static com.github.durex.support.RespConstant.OK;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.durex.music.model.PlayList;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.utils.Json;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PlaylistController.class)
class PlaylistControllerTest {
  @InjectMock PlaylistService playlistService;

  @Test
  void testGetPlaylistByTitle() {
    var playlistResp = DemoMusicData.givenSomePlayList(5);
    when(playlistService.findPlayListByTitle(anyString())).thenReturn(playlistResp);
    given()
        .contentType(APPLICATION_JSON)
        .queryParam("title", "test")
        .when()
        .get("/")
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.hasSize(5));
  }

  @Test
  void testGetPlayListById() {
    var playlistResp = DemoMusicData.givenAPlayList();
    when(playlistService.findPlayListById(anyString())).thenReturn(playlistResp);
    given()
        .contentType(APPLICATION_JSON)
        .pathParam("id", "test")
        .when()
        .get("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result.id", Matchers.equalTo(playlistResp.getId()));
  }

  @Test
  void testSavePlayList() throws IOException {
    var playlistResp = DemoMusicData.givenAPlayListMusic();
    when(playlistService.createPlaylist(any())).thenReturn(new int[] {1, 2, 3});
    given()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(playlistResp))
        .when()
        .post()
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.hasSize(3));
  }

  @Test
  void testUpdatePlayList() throws IOException {
    var playlist = DemoMusicData.givenAPlayList();
    when(playlistService.updatePlaylist(any(PlayList.class))).thenReturn(1);
    given()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(playlist))
        .when()
        .put()
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.equalTo(1));
  }

  @Test
  void testDeletePlayList() {
    when(playlistService.deletePlaylistById(anyString())).thenReturn(1);
    given()
        .when()
        .pathParam("id", "test")
        .delete("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.equalTo(1));
  }
}
