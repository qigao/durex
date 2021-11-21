package com.github.durex.basic.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.MysqlResources;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MysqlResources.class, parallel = true)
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
  @Order(10)
  void getMusics_without_editor_title_return200() {
    given().when().get("/v1/music").then().statusCode(200).body("music.size()", Matchers.is(1));
  }

  @Test
  @Order(20)
  void getMusics_with_editor_without_title_return200() {
    given()
        .when()
        .get("/v1/music?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("music.size()", Matchers.is(4));
  }

  @Test
  @Order(30)
  void getMusicByID_Return200() {
    given()
        .when()
        .get("/v1/music/1?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("id", Matchers.equalTo(1));
  }

  @Test
  @Order(40)
  void getMusicByID_without_editor_Return404() {
    given().when().get("/v1/music/5").then().statusCode(404);
  }

  @Test
  @Order(50)
  void getMusicByID_without_editor_Return200() {
    given().when().get("/v1/music/4").then().statusCode(200).body("id", Matchers.equalTo(4));
  }

  @Test
  void getMusicByID_with_editor_Return200() {
    given()
        .when()
        .get("/v1/music?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("$.size", Matchers.equalTo(4));
  }

  @Test
  void getMusicByID_with_title_editor_Return200() {
    given()
        .when()
        .get("/v1/music?editor=d1e5nqreqo&tile=周")
        .then()
        .statusCode(200)
        .body("$.size", Matchers.equalTo(4));
  }

  @Test
  void updateMusicByID_EditorID_Return200() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(
            "{\"id\":4,\"voice\":\"basic\",\"title\":\"The Cradle\",\"artist\":\"Schubert\",\"url\":\"http://localhost/music/demo.mp3\"}")
        .put("/v1/music/4?editor=d1e5nqreqo")
        .then()
        .statusCode(200);
    given()
        .when()
        .get("/v1/music/4?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("artist", Matchers.equalTo("Schubert"));
  }

  @Test
  void deleteMusicByID_Return204_Then_CheckByID_Return404() {
    given().when().delete("/v1/music/2?editor=d1e5nqreqo").then().statusCode(204);
    given().when().get("/v1/music/2?editor=d1e5nqreqo").then().statusCode(404);
  }

  @Test
  void createMusic_Return200() {
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(
            "{\"id\":5,\"voice\":\"basic\",\"title\":\"The Cradle\",\"artist\":\"Schubert\",\"url\":\"http://localhost/music/demo.mp3\"}")
        .post("/v1/music?editor=d1e5nqreqo")
        .then()
        .statusCode(200)
        .body("artist", Matchers.equalTo("Schubert"));
  }
}
