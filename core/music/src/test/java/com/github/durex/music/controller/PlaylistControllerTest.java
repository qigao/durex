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
  public static final String EDITOR = "?editor=81";

  public static final String PLAYLIST = "/v1/playlist/";
  public static final String PLAYLIST_WITH_EDITOR = "/v1/playlist" + EDITOR;
  public static final String MUSIC_WITH_EDITOR = "/v1/music" + EDITOR;
  public static final String RESULT = "result";
  public static final String MUSIC_ID_A = "abcd1234";
  public static final String MUSIC_ID_B = "abcd5678";
  public static final String MUSICS_ID = "data.musics.id";
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
                    .post(MUSIC_WITH_EDITOR)
                    .then()
                    .statusCode(200);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    given().when().get(MUSIC_WITH_EDITOR).then().statusCode(200).body("data.size()", equalTo(5));
    // Create playlist
    var requestJson = Data.givenPlayListWithInitialMusics();
    requestJson.setTitle("test");
    UNIQUE_ID =
        given()
            .when()
            .contentType(APPLICATION_JSON)
            .body(Json.toString(requestJson))
            .post(PLAYLIST_WITH_EDITOR)
            .then()
            .statusCode(200)
            .extract()
            .path("data")
            .toString();
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get(PLAYLIST_WITH_EDITOR + "&deviceId=abcd")
        .then()
        .log()
        .all()
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
        .post(MUSIC_WITH_EDITOR)
        .then()
        .statusCode(200);
    var music8 = Data.givenAMusic();
    music8.setId("8");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(music8))
        .post(MUSIC_WITH_EDITOR)
        .then()
        .statusCode(200);
    // Then add musics to playlist
    var musicIDList = MusicIDListRequest.builder().musics(List.of("7", "8")).build();
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .put(PLAYLIST + "update/" + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body(RESULT, equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body(MUSICS_ID, hasItems("7", "8"));
    // delete music from playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .delete(PLAYLIST + "update/" + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body("result", equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
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
        .post(MUSIC_WITH_EDITOR)
        .then()
        .statusCode(200);
    var musicB = Data.givenAMusic();
    musicB.setId(MUSIC_ID_B);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicB))
        .post(MUSIC_WITH_EDITOR)
        .then()
        .statusCode(200);
    var musicAWithOrderA = OrderedMusicRequest.builder().musicId(MUSIC_ID_A).order(1).build();
    var musicBWithOrderB = OrderedMusicRequest.builder().musicId(MUSIC_ID_B).order(3).build();
    // update playlist by order/${id}
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(List.of(musicAWithOrderA, musicBWithOrderB)))
        .put(PLAYLIST + "order/" + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body(RESULT, equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .log()
        .all()
        .statusCode(200)
        .body(MUSICS_ID, hasItems(MUSIC_ID_A, MUSIC_ID_B))
        .body("data.musics.order", hasItems(1, 2));
    var musicIDList = MusicIDListRequest.builder().musics(List.of(MUSIC_ID_A, MUSIC_ID_B)).build();
    // delete playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicIDList))
        .delete(PLAYLIST + "update/" + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body("result", equalTo(200));
    // check playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
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
        .put(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200);
    // check the playlist title
    given()
        .when()
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body("data.title", equalTo("aaa"));
    // delete playlist
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .delete(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200);
    // check playlist
    given()
        .header("X-AUTH-KEY", "wrong-key")
        .when()
        .get(PLAYLIST + UNIQUE_ID + EDITOR)
        .then()
        .statusCode(200)
        .body("result", equalTo(404));
  }
}
