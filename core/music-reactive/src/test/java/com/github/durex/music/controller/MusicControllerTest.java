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

import com.github.durex.music.model.Music;
import com.github.durex.music.service.MusicService;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.utils.Json;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@QuarkusTest
@TestHTTPEndpoint(MusicController.class)
class MusicControllerTest {

  @InjectMock MusicService service;

  @Test
  void testGetMusicByTitle() {
    var musics = DemoMusicData.givenSomeMusics(5);
    when(service.getMusicsByTitle(any())).thenReturn(Flux.fromIterable(musics));
    given()
        .when()
        .queryParam("title", "demo")
        .get()
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", hasSize(5));
  }

  @Test
  void testGetMusicById() {
    var music = DemoMusicData.givenAMusic();
    when(service.getMusicById(anyString())).thenReturn(Mono.just(music));
    given()
        .pathParam("id", music.getId())
        .contentType(APPLICATION_JSON)
        .when()
        .get("/{id}")
        .then()
      .log().all()
        .statusCode(200)
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result.id", equalTo(music.getId()));
  }

  @Test
  void testDeleteMusic() {
    when(service.deleteMusicById(anyString())).thenReturn(Mono.just(1));
    given()
        .when()
        .pathParam("id", UniqID.getId())
        .delete("/{id}")
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", equalTo(1));
  }

  @Test
  void testCreateMusic() throws IOException {
    when(service.createMusic(any(Music.class))).thenReturn(Mono.just(1));
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(DemoMusicData.givenAMusic()))
        .post()
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", equalTo(1));
  }

  @Test
  void testUpdateMusic() throws IOException {
    when(service.updateMusic(any(Music.class))).thenReturn(Mono.just(1));
    given()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(DemoMusicData.givenAMusic()))
        .when()
        .put()
        .then()
        .body("error.errorCode", equalTo(NOTHING_FAILED))
        .body("error.message", equalTo(OK))
        .body("result", equalTo(1));
  }
}
