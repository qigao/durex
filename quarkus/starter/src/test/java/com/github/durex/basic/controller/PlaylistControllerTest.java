package com.github.durex.basic.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.support.MockedMysql;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
class PlaylistControllerTest {
  public static final String JSON_DATA_DIR = "src/test/resources/json";
  public static final String PLAYLIST_JSON = JSON_DATA_DIR + "/playlist/demo.json";
  public static final String MUSIC_JSON = JSON_DATA_DIR + "/music/music.json";
  static MockedMysql mysql = new MockedMysql();

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
  void testGetPlaylist_Return404() {
    given().when().get("/v1/playlist/100").then().statusCode(404);
  }

  @Test
  @Order(120)
  void testCreatePlayList_Return200() throws IOException {
    var requestJson = Json.read(Paths.get(PLAYLIST_JSON).toFile(), PlayListRequest.class);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toBytes(requestJson))
        .post("/v1/playlist?editor=12e2d3")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(130)
  void testGetPlaylist_Return200() {
    given()
        .when()
        .get("/v1/playlist/1")
        .then()
        .statusCode(200)
        .body("total", equalTo(3), "duration", equalTo(3 * 200), "musics.size()", equalTo(3));
  }

  @Test
  @Order(135)
  void createMusic_Return200() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Paths.get(MUSIC_JSON).toFile())
        .post("/v1/music?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("artist", Matchers.equalTo("Schubert"));
  }

  @Test
  @Order(140)
  void testAdd1MusicToPlaylist_Return200() throws IOException {
    var requestJson = Json.read(Paths.get(PLAYLIST_JSON).toFile(), PlayListRequest.class);
    var newMusic = Json.read(Paths.get(MUSIC_JSON).toFile(), MusicRequest.class);
    requestJson.getMusics().add(newMusic);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toBytes(requestJson))
        .put("/v1/playlist/1?editor=12e2d3")
        .then()
        .statusCode(200)
        .body("total", equalTo(4), "duration", equalTo(4 * 200), "musics.size()", equalTo(4));
  }

  @Test
  @Order(145)
  void testRemove1MusicToPlaylist_Return200() throws IOException {
    var requestJson = Json.read(Paths.get(PLAYLIST_JSON).toFile(), PlayListRequest.class);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toBytes(requestJson))
        .put("/v1/playlist/1?editor=12e2d3")
        .then()
        .statusCode(200)
        .body("total", equalTo(3), "duration", equalTo(3 * 200), "musics.size()", equalTo(3));
  }

  @Test
  @Order(150)
  void testDeletePlaylist_Return200() {
    given().when().delete("/v1/playlist/1").then().statusCode(204);
  }

  @Test
  void testGetAllPlaylist_Return200() throws IOException {
    var requestJson = Json.read(Paths.get(PLAYLIST_JSON).toFile(), PlayListRequest.class);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toBytes(requestJson))
        .post("/v1/playlist?editor=12e2d3")
        .then()
        .statusCode(200);
    given()
        .when()
        .get("/v1/playlist?editor=12e2d3")
        .then()
        .statusCode(200)
        .body("[0].musics.size()", equalTo(3))
        .body("[0].duration", equalTo(3 * 200))
        .body("[0].total", equalTo(3))
        .body("[0].musics.title", hasItems("爱情转移"));
  }

  @Test
  @Order(160)
  void testUpdatePlaylist_return200() throws IOException {
    var jsonFile = JSON_DATA_DIR + "/playlist/real.json";
    var musicList = JSON_DATA_DIR + "/music/music5.json";
    var musicListRequest =
        Json.read(Paths.get(musicList).toFile(), new TypeReference<List<MusicRequest>>() {});
    musicListRequest.forEach(
        music -> {
          try {
            given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(Json.toString(music))
                .post("/v1/music?editor=d1e5nqreqo")
                .then()
                .statusCode(200);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(requestJson))
        .post("/v1/playlist?editor=12e2d3")
        .then()
        .statusCode(200);
    var updateRequest = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    updateRequest.setCoverUrl("http://localhost/music/demo.jpeg");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(updateRequest))
        .put("/v1/playlist/1tVNDNP3YfqCOH7wKXStcEc61UP")
        .then()
        .statusCode(200);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .get("/v1/playlist?editor=12e2d3")
        .then()
        .statusCode(200)
        .body("[0].coverUrl", equalTo("http://localhost/music/demo.jpeg"));
  }
}
