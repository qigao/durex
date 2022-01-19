package com.github.durex.music.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class, parallel = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusicServiceTest {
  public static final String EDITOR_ID = "d1e5nqreqo";

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
  @Order(12)
  void getMusicsByEditorAndDevice() {
    var music = musicService.getMusicsByEditorAndDevice(EDITOR_ID, "a001", 0, 5);
    assertEquals(3, music.size());
  }

  @Test
  @Order(15)
  void createMusicEditorIsNULL() throws IOException, NotFoundException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId("60");
    musicRequest.setTitle("爱情转移");
    musicService.createMusic(null, musicRequest);

    assertEquals("60", musicService.getMusicByIdAndEditor("60", null).getId());
  }

  @Test
  @Order(20)
  void getMusicsByEditorAndEditorNull() {
    var music = musicService.getMusicsByEditor(null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(30)
  void getMusicsByTitleAndEditorEditorIsNULL() {
    var music = musicService.getMusicsByTitleAndEditor("情", null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(40)
  void getMusicsByTitleAndEditorEditorTitleBothNULL() {
    var music = musicService.getMusicsByTitleAndEditor(null, null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(40)
  void getMusicsByTitleAndEditorTitleIsEmpty() {
    var music = musicService.getMusicsByTitleAndEditor("", null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(50)
  void getMusicsByTitleAndEditorIsNULLTitleAll() {
    var music = musicService.getMusicsByTitleAndEditor("*", null, 0, 5);
    assertEquals(0, music.size());
  }

  @Test
  @Order(60)
  void getMusicsByTitleAndEditorIsNULL() {
    var music = musicService.getMusicsByTitleAndEditor("爱", null, 0, 5);
    assertEquals(1, music.size());
  }

  @Test
  @Order(60)
  void getMusicsByTitleAndEditorIsNotNULL() {
    var music = musicService.getMusicsByTitleAndEditor("爱", EDITOR_ID, 0, 5);
    assertEquals(3, music.size());
  }

  @Test
  @Order(80)
  void createMusic() throws IOException, NotFoundException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId("50");
    musicService.createMusic(EDITOR_ID, musicRequest);
    assertEquals("50", musicService.getMusicByIdAndEditor("50", EDITOR_ID).getId());
  }

  @Test
  @Order(100)
  void updateMusic() throws IOException, NotFoundException {
    var musicRequest = Data.givenAMusic();
    musicRequest.setId("3");
    musicRequest.setDuration(256);
    musicService.updateMusic("3", EDITOR_ID, musicRequest);

    assertEquals("Schubert", musicService.getMusicByIdAndEditor("3", EDITOR_ID).getArtist());
    assertEquals(256, musicService.getMusicByIdAndEditor("3", EDITOR_ID).getDuration());
  }
}
