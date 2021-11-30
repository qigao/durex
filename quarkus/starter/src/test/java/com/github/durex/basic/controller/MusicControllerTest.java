package com.github.durex.basic.controller;

import com.github.durex.basic.model.MusicRequest;
import com.github.durex.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
class MusicControllerTest {
  public static final String EDITOR_PARAM = "editor=d1e5nqreqo";
  public static final String V1_MUSIC = "/v1/music";
  public static final String SCHUBERT = "Schubert";
  public static final String MUSIC_JSON = "src/test/resources/json/music/music.json";
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
  @Order(30)
  void getMusicByID_Return200() {
    given()
        .when()
        .header("x-auth", "this is a test")
        .get(V1_MUSIC + "/1?" + EDITOR_PARAM)
        .then()
        .statusCode(200)
        .body("id", equalTo("1"));
  }

  @Test
  @Order(40)
  void getMusicByID_without_editor_Return404() {
    given().when().get(V1_MUSIC + "/" + "5").then().statusCode(404);
  }

  @Test
  @Order(50)
  void getMusicByID_without_editor_Return200() {
    given().when().get(V1_MUSIC + "/4").then().statusCode(200).body("id", equalTo("4"));
  }

  @Test
  @Order(60)
  void getMusicByID_with_editor_Return200() {
    given().when().get(V1_MUSIC).then().statusCode(200);
  }

  @Test
  @Order(70)
  void getMusicByID_with_title_editor_Return200() {
    given().when().get(V1_MUSIC + "?" + EDITOR_PARAM + "&title=爱").then().statusCode(200);
  }

  @Test
  @Order(80)
  void updateMusicByID_EditorID_Return200() throws IOException {
    var musicRequest = Json.read(Paths.get(MUSIC_JSON).toFile(), MusicRequest.class);
    musicRequest.setId("4");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicRequest))
        .put(V1_MUSIC + "/4?" + EDITOR_PARAM)
        .then()
        .statusCode(200);
    given()
        .when()
        .get(V1_MUSIC + "/4?" + EDITOR_PARAM)
        .then()
        .statusCode(200)
        .body("artist", equalTo(SCHUBERT));
  }

  @Test
  @Order(90)
  void deleteMusicByID_Return204_Then_CheckByID_Return404() {
    given().when().delete(V1_MUSIC + "/2?" + EDITOR_PARAM).then().statusCode(204);
    given().when().get(V1_MUSIC + "/2?" + EDITOR_PARAM).then().statusCode(404);
  }

  @Test
  @Order(100)
  void createMusic_Return200() throws IOException {
    var musicRequest = Json.read(Paths.get(MUSIC_JSON).toFile(), MusicRequest.class);
    musicRequest.setId("wtVNDNP3YfqCOH7wKXStcEc61U1");
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicRequest))
        .post(V1_MUSIC + "?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("artist", equalTo(SCHUBERT));
    given()
        .when()
        .get(V1_MUSIC + "/wtVNDNP3YfqCOH7wKXStcEc61U1?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("artist", equalTo(SCHUBERT));
    given()
        .when()
        .get(V1_MUSIC + "?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("size()", Matchers.equalTo(1))
        .body("artist", Matchers.hasItems(SCHUBERT));
  }
}
