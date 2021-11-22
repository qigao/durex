package com.github.durex.basic.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;

import com.github.durex.MysqlResources;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Paths;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(value = MysqlResources.class, parallel = true)
class PlaylistControllerTest {
  static MysqlResources mysql = new MysqlResources();

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
    given().when().get("/v1/playlist/100").then().statusCode(400);
  }

  @Test
  @Order(120)
  void testCreatePlayList_Return200() throws IOException {
    var jsonFile = "src/test/resources/json/playlist.json";
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
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
        .body(
            "{\"id\":6,\"voice\":\"basic\",\"title\":\"The Cradle\",\"artist\":\"Mozart\",\"url\":\"http://localhost/music/demo.mp3\"}")
        .post("/v1/music?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("artist", Matchers.equalTo("Mozart"));
  }

  @Test
  @Order(140)
  void testAdd1MusicToPlaylist_Return200() throws IOException {
    var jsonFile = "src/test/resources/json/playlist.json";
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    var newMusicJson =
        "{\"id\":\"6\",\"voice\":\"basic\",\"duration\":200,\"title\":\"The Cradle\",\"artist\":\"Mozart\",\"url\":\"http://localhost/music/demo.mp3\"}";
    var newMusic = Json.read(newMusicJson, MusicRequest.class);
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
    var jsonFile = "src/test/resources/json/playlist.json";
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
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
}
