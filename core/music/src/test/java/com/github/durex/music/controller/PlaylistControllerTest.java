package com.github.durex.music.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import com.github.durex.music.model.MusicIDListRequest;
import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaylistControllerTest {

  public static final String V1_PLAYLIST = "/v1/playlist/";
  public static final String V1_MUSIC = "/v1/music";
  public static final String RESULT = "result";
  public static final String MUSIC_ID_A = "abcd1234";
  public static final String MUSIC_ID_B = "abcd5678";
  public static final String MUSICS_ID = "data.musics.id";
  public static final String EDITOR = "editor";
  public static final String EDITOR_81 = "81";
  public static final String DEVICE_ID = "deviceId";
  public static final String ID = "id";
  static MockedMysql mysql = new MockedMysql();
  private String UNIQUE_ID = "";

  @BeforeAll
  static void setUp() {
    mysql.start();
  }

  @AfterAll
  static void tearDown() {
    mysql.stop();
  }

  @Test
  @Order(100)
  void testCreateMusicsAndPlaylistReturn200() throws IOException {
    Data.givenFiveMusics()
        .forEach(
            music -> {
              try {
                given()
                    .when()
                    .contentType(APPLICATION_JSON)
                    .body(Json.toString(music))
                    .queryParam(EDITOR, EDITOR_81)
                    .post(V1_MUSIC)
                    .then()
                    .statusCode(200);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    given()
        .when()
        .queryParam(EDITOR, EDITOR_81)
        .get(V1_MUSIC)
        .then()
        .statusCode(200)
        .body("data.size()", equalTo(5));
    // Create playlist
    var requestJson = Data.givenPlayListWithInitialMusics();
    requestJson.setTitle("test");
    UNIQUE_ID =
        given()
            .when()
            .contentType(APPLICATION_JSON)
            .body(Json.toString(requestJson))
            .queryParam(EDITOR, EDITOR_81)
            .post(V1_PLAYLIST)
            .then()
            .statusCode(200)
            .extract()
            .path("data")
            .toString();
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .queryParam(DEVICE_ID, "abcd")
        .get(V1_PLAYLIST)
        .then()
        .statusCode(200)
        .body("data.title", hasItems("test"));
  }

  @Test
  @Order(120)
  void testUpdatePlaylistInBatch() throws IOException {
    // Given 2 musics
    var music7 = Data.givenAMusic();
    music7.setId("7");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(music7))
        .queryParam(EDITOR, EDITOR_81)
        .post(V1_MUSIC)
        .then()
        .statusCode(200);
    var music8 = Data.givenAMusic();
    music8.setId("8");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(music8))
        .queryParam(EDITOR, EDITOR_81)
        .post(V1_MUSIC)
        .then()
        .statusCode(200);
    // Then add musics to playlist
    var musicIDList = MusicIDListRequest.builder().musics(List.of("7", "8")).build();
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .put(V1_PLAYLIST + "update/{id}")
        .then()
        .statusCode(200)
        .body(RESULT, equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body(MUSICS_ID, hasItems("7", "8"));
    // delete music from playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .delete(V1_PLAYLIST + "update/{id}")
        .then()
        .statusCode(200)
        .body("result", equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body(MUSICS_ID, not(hasItems("7", "8")));
  }

  @Test
  @Order(170)
  void testUpdatePlaylistWithOrderInBatch() throws IOException {
    // given 2 musics
    var musicA = Data.givenAMusic();
    musicA.setId(MUSIC_ID_A);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicA))
        .queryParam(EDITOR, EDITOR_81)
        .post(V1_MUSIC)
        .then()
        .statusCode(200);
    var musicB = Data.givenAMusic();
    musicB.setId(MUSIC_ID_B);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicB))
        .queryParam(EDITOR, EDITOR_81)
        .post(V1_MUSIC)
        .then()
        .statusCode(200);
    var musicAWithOrderA = OrderedMusicRequest.builder().musicId(MUSIC_ID_A).order(1).build();
    var musicBWithOrderB = OrderedMusicRequest.builder().musicId(MUSIC_ID_B).order(3).build();
    // update playlist by order/${id}
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(List.of(musicAWithOrderA, musicBWithOrderB)))
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .put(V1_PLAYLIST + "order/{id}")
        .then()
        .statusCode(200)
        .body(RESULT, equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body(MUSICS_ID, hasItems(MUSIC_ID_A, MUSIC_ID_B))
        .body("data.musics.order", hasItems(1, 2));
    var musicIDList = MusicIDListRequest.builder().musics(List.of(MUSIC_ID_A, MUSIC_ID_B)).build();
    // delete playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .delete(V1_PLAYLIST + "update/{id}")
        .then()
        .statusCode(200)
        .body("result", equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body(MUSICS_ID, not(hasItems(MUSIC_ID_A, MUSIC_ID_B)));
  }

  @Test
  @Order(180)
  void testDeletePlaylistById() throws IOException {
    // update playlist by order/${id}
    var requestJson = Data.givenPlayListWithInitialMusics();
    requestJson.setTitle("aaa");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(requestJson))
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .put(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200);
    // check the playlist title
    given()
        .when()
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body("data.title", equalTo("aaa"));
    // delete playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .delete(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200);
    // check playlist
    given()
        .header("X-AUTH-KEY", "wrong-key")
        .when()
        .queryParam(EDITOR, EDITOR_81)
        .pathParam(ID, UNIQUE_ID)
        .get(V1_PLAYLIST + "{id}")
        .then()
        .statusCode(200)
        .body("result", equalTo(404));
  }
}
