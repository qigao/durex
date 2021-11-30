package com.github.durex.basic.service;

import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.basic.model.MusicRequest;
import com.github.durex.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
class MusicServiceTest {
  static MockedMysql mysql = new MockedMysql();
  @Inject MusicService musicServiceService;

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
  void getMusicsByEditor() {
    var music = musicServiceService.getMusicsByEditor("d1e5nqreqo", 0, 5);
    assertEquals(4, music.size());
  }

  @Test
  @Order(20)
  void getMusicsByEditor_editor_null() {
    var music = musicServiceService.getMusicsByEditor(null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(30)
  void getMusicsByTitleAndEditor_editor_isNULL() {
    var music = musicServiceService.getMusicsByTitleAndEditor("情", null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(40)
  void getMusicsByTitleAndEditor_editor_title_bothNULL() {
    var music = musicServiceService.getMusicsByTitleAndEditor(null, null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(50)
  void getMusicsByTitleAndEditor_editor_isNULL_title_all() {
    var music = musicServiceService.getMusicsByTitleAndEditor("*", null, 0, 5);
    assertEquals(0, music.size());
  }

  @Test
  @Order(60)
  void getMusicsByTitleAndEditor_editor_isNULL_title_ai() {
    var music = musicServiceService.getMusicsByTitleAndEditor("爱", null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(60)
  void getMusicsByTitleAndEditor_editor_isNotNULL() {
    var music = musicServiceService.getMusicsByTitleAndEditor("爱", "d1e5nqreqo", 0, 5);
    assertEquals(4, music.size());
  }

  @Test
  @Order(80)
  void createMusic() throws IOException, NotFoundException {
    var json =
        "{\"id\":\"5\",\"title\":\"The Cradle\",\"artist\":\"Schubert\",\"playUrl\":\"http://localhost/music/demo.mp3\"}";
    var musicRequest = Json.read(json, MusicRequest.class);
    var music = musicServiceService.createMusic("d1e5nqreqo", musicRequest);
    assertEquals("5", musicServiceService.getMusicByIdAndEditor("5", "d1e5nqreqo").getId());
  }

  @Test
  @Order(90)
  void createMusicEditorIsNULL() throws IOException, NotFoundException {
    var json =
        "{\"id\":\"6\",\"title\":\"The Cradle\",\"artist\":\"Schubert\",\"playUrl\":\"http://localhost/music/demo.mp3\"}";
    var musicRequest = Json.read(json, MusicRequest.class);
    var music = musicServiceService.createMusic("d1e5nqreqo", musicRequest);
    assertEquals("6", musicServiceService.getMusicByIdAndEditor("6", null).getId());
  }

  @Test
  @Order(100)
  void updateMusic() throws IOException, NotFoundException {
    var json =
        "{\"id\":\"3\",\"title\":\"The Cradle\",\"artist\":\"Mozart\",\"playUrl\":\"http://localhost/music/demo.mp3\"}";
    var musicRequest = Json.read(json, MusicRequest.class);
    var music = musicServiceService.updateMusic("3", "d1e5nqreqo", musicRequest);
    assertEquals(
        "Mozart", musicServiceService.getMusicByIdAndEditor("3", "d1e5nqreqo").getArtist());
  }
}
