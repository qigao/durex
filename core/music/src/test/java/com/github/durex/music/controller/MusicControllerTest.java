package com.github.durex.music.controller;

import static com.github.durex.support.RespConstant.APPLICATION_JSON;
import static com.github.durex.support.RespConstant.NOTHING_FAILED;
import static com.github.durex.support.RespConstant.OK;
import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.durex.music.api.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.utils.Json;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(MusicController.class)
class MusicControllerTest {
  @InjectMock MusicService service;

  @Test
  void testGetMusicByTitle() {
    when(service.getMusicsByTitle(any())).thenReturn(DemoMusicData.givenSomeMusics(5));
    given()
        .when()
        .queryParam("title", "demo")
        .get()
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.hasSize(5));
  }

  @Test
  void testGetMusicById() {
    var music = DemoMusicData.givenAMusic();
    when(service.getMusicById(anyString())).thenReturn(music);
    given()
        .when()
        .pathParam("id", music.getId())
        .get("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.hasSize(1));
  }

  @Test
  void testDeleteMusic() {
    when(service.deleteMusicById(anyString())).thenReturn(1);
    given()
        .when()
        .pathParam("id", UniqID.getId())
        .delete("/{id}")
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.equalTo(1));
  }

  @Test
  void testCreateMusic() throws IOException {
    when(service.createMusic(any(Music.class))).thenReturn(1);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(DemoMusicData.givenAMusic()))
        .post()
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.equalTo(1));
  }

  @Test
  void testUpdateMusic() throws IOException {
    when(service.updateMusic(any(Music.class))).thenReturn(1);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(DemoMusicData.givenAMusic()))
        .put()
        .then()
        .body("error.errorCode", Matchers.equalTo(NOTHING_FAILED))
        .body("error.message", Matchers.equalTo(OK))
        .body("result", Matchers.equalTo(1));
  }
}
