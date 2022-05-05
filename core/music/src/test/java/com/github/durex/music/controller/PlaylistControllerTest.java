package com.github.durex.music.controller;

import static io.restassured.RestAssured.given;
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
        .when()
        .queryParam("title", "test")
        .get("/")
        .then()
        .body("error.errorCode", Matchers.equalTo("NOTHING_FAILED"))
        .body("error.message", Matchers.equalTo("OK"))
        .body("data", Matchers.hasSize(5));
  }

  @Test
  void testGetPlayListById() {
    var playlistResp = DemoMusicData.givenAPlayList();
    when(playlistService.findPlayListById(anyString())).thenReturn(playlistResp);
    given()
        .when()
        .pathParam("id", "test")
        .get("/{id}")
        .then()
        .log()
        .all()
        .body("error.errorCode", Matchers.equalTo("NOTHING_FAILED"))
        .body("error.message", Matchers.equalTo("OK"))
        .body("data", Matchers.hasSize(1));
  }

  @Test
  void testSavePlayList() throws IOException {
    var playlistResp = DemoMusicData.givenAPlayListMusic();
    when(playlistService.createPlaylist(any())).thenReturn(new int[] {1, 2, 3});
    given()
        .when()
        .contentType("application/json")
        .body(Json.toString(playlistResp))
        .post()
        .then()
        .body("error.errorCode", Matchers.equalTo("NOTHING_FAILED"))
        .body("error.message", Matchers.equalTo("OK"))
        .body("data", Matchers.hasSize(3));
  }

  @Test
  void testUpdatePlayList() throws IOException {
    var playlist = DemoMusicData.givenAPlayList();
    when(playlistService.updatePlaylist(any(PlayList.class))).thenReturn(1);
    given()
        .when()
        .pathParam("id", "test")
        .contentType("application/json")
        .body(Json.toString(playlist))
        .put("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo("NOTHING_FAILED"))
        .body("error.message", Matchers.equalTo("OK"))
        .body("data", Matchers.equalTo(1));
  }

  @Test
  void testDeletePlayList() {
    when(playlistService.deletePlaylistById(anyString())).thenReturn(1);
    given()
        .when()
        .pathParam("id", "test")
        .delete("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo("NOTHING_FAILED"))
        .body("error.message", Matchers.equalTo("OK"))
        .body("data", Matchers.equalTo(1));
  }
}
