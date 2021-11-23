package com.github.durex.basic.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.*;

import com.github.durex.MysqlResources;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.file.Paths;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MysqlResources.class)
class MusicControllerTest {
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
  @Order(30)
  void getMusicByID_Return200() {
    given()
        .when()
        .get("/v1/music/1?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("id", equalTo("1"));
  }

  @Test
  @Order(40)
  void getMusicByID_without_editor_Return404() {
    given().when().get("/v1/music/5").then().statusCode(404);
  }

  @Test
  @Order(50)
  void getMusicByID_without_editor_Return200() {
    given().when().get("/v1/music/4").then().statusCode(200).body("id", equalTo("4"));
  }

  @Test
  @Order(60)
  void getMusicByID_with_editor_Return200() {
    given().when().get("/v1/music").then().statusCode(200);
  }

  @Test
  @Order(70)
  void getMusicByID_with_title_editor_Return200() {
    given().when().get("/v1/music?editor=d1e5nqreqo&title=爱").then().statusCode(200);
  }

  @Test
  @Order(80)
  void updateMusicByID_EditorID_Return200() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(
            "{\"id\":\"4\",\"title\":\"The Cradle\",\"artist\":\"Schubert\",\"playUrl\":\"http://localhost/music/demo.mp3\"}")
        .put("/v1/music/4?editor=d1e5nqreqo")
        .then()
        .statusCode(200);
    given()
        .when()
        .get("/v1/music/4?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("artist", equalTo("Schubert"));
  }

  @Test
  @Order(90)
  void deleteMusicByID_Return204_Then_CheckByID_Return404() {
    given().when().delete("/v1/music/2?editor=d1e5nqreqo").then().statusCode(204);
    given().when().get("/v1/music/2?editor=d1e5nqreqo").then().statusCode(404);
  }

  @Test
  @Order(100)
  void createMusic_Return200() {
    var music = "src/test/resources/json/music/music1.json";
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Paths.get(music).toFile())
        .post("/v1/music?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("artist", equalTo("Schubert"));
    given()
        .when()
        .get("/v1/music/wtVNDNP3YfqCOH7wKXStcEc61U1?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("artist", equalTo("Schubert"));
    given()
        .when()
        .get("/v1/music?editor=1231dfasdf234")
        .then()
        .statusCode(200)
        .body("size()", Matchers.equalTo(1))
        .body("artist", Matchers.hasItems("Schubert"));
  }
}
