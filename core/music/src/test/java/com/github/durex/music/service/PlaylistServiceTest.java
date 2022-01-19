package com.github.durex.music.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import javax.inject.Inject;
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
class PlaylistServiceTest {
  public static final String EDITOR = "d1e5nqreqo";
  private String UNIQUE_ID = "";

  static MockedMysql mysql = new MockedMysql();
  @Inject PlaylistService playlistService;
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
  void createPlaylist() throws IOException {
    var requestJson = Data.givenPlayListWithInitialMusics();
    UNIQUE_ID = playlistService.createPlaylist(null, requestJson);
    assertNotNull(UNIQUE_ID);
  }

  @Test
  @Order(15)
  void testCreatePlaylistWithID() throws IOException {
    var playListRequest = Data.givenPlayListWithInitialMusics();
    playListRequest.setId("2");
    playlistService.createPlaylistWithID(EDITOR, playListRequest);
    var playlist2 = playlistService.findPlayListById("2");
    assertEquals("2", playlist2.getId());
  }

  @Test
  @Order(16)
  void testCreatePlaylistWithoutID() throws IOException {
    var playListRequest = Data.givenPlayListWithInitialMusics();
    UNIQUE_ID = playlistService.createPlaylist(EDITOR, playListRequest);
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    assertEquals(UNIQUE_ID, playlist.getId());
  }

  @Test
  @Order(20)
  void testGetPlayListByEditorAndDevice() {
    var playlist = playlistService.findPlayListByEditorAndDevice(EDITOR, "abcd", 0, 5);
    assertEquals(2, playlist.size());
  }

  @Test
  @Order(60)
  void testGetPlaylistWithoutEditorAndTitle() {
    var playlist = playlistService.getPlayListByEditorAndTitle(null, null, 0, 5);
    assertEquals(0, playlist.size());
  }

  @Test
  @Order(70)
  void testGetPlaylistByEditorAndTitle() {
    var playlist = playlistService.getPlayListByEditorAndTitle("The Cradles", EDITOR, 0, 5);
    assertEquals(2, playlist.size());
  }

  @Test
  @Order(80)
  void testGetPlaylistByEditorAndTitleWildCardTitle() {
    var playlist = playlistService.getPlayListByEditorAndTitle("C", EDITOR, 0, 5);
    assertEquals(2, playlist.size());
  }

  //  @Test
  //  @Order(90)
  //  void testCreatePlaylistThenUpdate() throws IOException {
  //    var musicListRequest = Data.givenFiveMusics();
  //    musicListRequest.forEach(music ->{ musicService.createMusic(EDITOR, music);});
  //    var playListRequest = Data.givenPlayListWithFiveMusics();
  //    playListRequest.setCoverUrl("http://localhost/music/demo.jpeg");
  //    UNIQUE_ID = playlistService.createPlaylist(EDITOR, playListRequest);
  //    var playlist = playlistService.findPlayListById(UNIQUE_ID);
  //    assertEquals("http://localhost/music/demo.jpeg", playlist.getCoverUrl());
  //  }

  @Test
  @Order(100)
  void testDeletePlayListByIdAndEditor() {
    assertEquals(1, playlistService.deletePlayListByIdAndEditor(UNIQUE_ID, EDITOR));
  }
}
