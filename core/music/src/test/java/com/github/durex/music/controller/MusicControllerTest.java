package com.github.durex.music.controller;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import javax.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusicControllerTest {
  public static final String V1_MUSIC = "/v1/music/";
  public static final String SCHUBERT = "Schubert";
  public static final String DATA_ARTIST = "data.artist";
  public static final String RESPONSE_RESULT = "result";
  public static final String EDITOR_ID = "d1e5nqreqo";

  public static final String MUSIC_ID = "wtVNDNP3YfqCOH7wKXStcEc61U1";
  public static final String SUCCESS = "success";
  public static final String DATA_ID = "data.id";
  public static final String DATA_VOICE_NAME = "data.voiceName";
  public static final String MSG = "msg";
  public static final String EDITOR = "editor";
  public static final String ID = "id";
  public static final String EDITOR_ID2 = "1231dfasdf234";
  static MockedMysql mysql = new MockedMysql();
  @Inject MusicService musicService;

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
  void getMusicsByEditor() throws IOException {
    Data.givenMusicsWithChineseName().forEach(music -> musicService.createMusic(EDITOR_ID, music));
    var music = musicService.getMusicsByEditor(EDITOR_ID, 0, 5);
    assertEquals(3, music.size());
  }

  @Test
  @Order(30)
  void getMusicByIDReturn200() {

    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "1")
        .get(V1_MUSIC + "{id}")
        .then()
        .body(RESPONSE_RESULT, Matchers.equalTo(200))
        .body(MSG, Matchers.equalTo(SUCCESS))
        .body(DATA_ID, Matchers.equalTo("1"));
  }

  @Test
  @Order(40)
  void getMusicByIDWithoutEditorReturn404() {
    given()
        .when()
        .get(V1_MUSIC + "5")
        .then()
        .statusCode(200)
        .body(RESPONSE_RESULT, Matchers.equalTo(404));
  }

  @Test
  @Order(42)
  void getMusicByEditorAndDeviceReturn200() {
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "1")
        .get(V1_MUSIC + "{id}")
        .then()
        .body(RESPONSE_RESULT, Matchers.equalTo(200))
        .body(MSG, Matchers.equalTo(SUCCESS))
        .body(DATA_ID, Matchers.equalTo("1"));
  }

  @Test
  @Order(50)
  void getMusicByIDWithoutEditorReturn200() throws IOException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId("60");
    musicRequest.setTitle("爱情转移");
    musicService.createMusic(null, musicRequest);
    given()
        .when()
        .get(V1_MUSIC + "60")
        .then()
        .statusCode(200)
        .body(DATA_ID, equalTo("60"))
        .body(DATA_VOICE_NAME, equalTo("Mum's voice"));
  }

  @Test
  @Order(60)
  void getMusicByIDWithoutEditorIDReturn200() {
    given().when().get(V1_MUSIC).then().statusCode(200);
  }

  @Test
  @Order(70)
  void getMusicByIDWithTitleEditorReturn200() {
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .queryParam("title", "爱移")
        .get(V1_MUSIC)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(80)
  void updateMusicByIDEditorIDReturn200() throws IOException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId("3");
    musicRequest.setDuration(256);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicRequest))
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "3")
        .put(V1_MUSIC + "{id}")
        .then()
        .statusCode(200);
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "3")
        .get(V1_MUSIC + "{id}")
        .then()
        .statusCode(200)
        .body(DATA_ARTIST, equalTo(SCHUBERT));
  }

  @Test
  @Order(90)
  void deleteMusicByIDReturn200ThenCheckByIDReturn404() {
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "3")
        .delete(V1_MUSIC + "{id}")
        .then()
        .statusCode(200);
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID)
        .pathParam(ID, "3")
        .get(V1_MUSIC + "{id}")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(100)
  void createMusicReturn200() throws IOException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId(MUSIC_ID);
    given()
        .when()
        .contentType(APPLICATION_JSON)
        .body(Json.toString(musicRequest))
        .queryParam(EDITOR, EDITOR_ID2)
        .post(V1_MUSIC)
        .then()
        .statusCode(200)
        .body(RESPONSE_RESULT, equalTo(200));
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID2)
        .pathParam(ID, MUSIC_ID)
        .get(V1_MUSIC + "{id}")
        .then()
        .statusCode(200)
        .body(DATA_ARTIST, equalTo(SCHUBERT));
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID2)
        .queryParam("deviceId", "12345678")
        .get(V1_MUSIC)
        .then()
        .statusCode(200)
        .body(DATA_ARTIST, Matchers.hasItems(SCHUBERT));
    given()
        .when()
        .queryParam(EDITOR, EDITOR_ID2)
        .get(V1_MUSIC)
        .then()
        .statusCode(200)
        .body(DATA_ARTIST, hasItems(SCHUBERT));
  }
}
