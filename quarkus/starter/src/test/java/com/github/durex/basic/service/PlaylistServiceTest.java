package com.github.durex.basic.service;

import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.MysqlResources;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MysqlResources.class, parallel = true)
class PlaylistServiceTest {
  static MysqlResources mysql = new MysqlResources();
  @Inject PlaylistService playlistService;

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
    var jsonFile = "src/test/resources/json/playlist.json";
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    var playlist = playlistService.createPlaylist(null, requestJson);
    assertEquals("1", playlist.getId());
  }

  @Test
  @Order(15)
  void testCreatePlaylistWithSameName() throws IOException {
    var jsonFile = "src/test/resources/json/playlist.json";
    var playListRequest = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    playListRequest.setId("2");
    var playlist = playlistService.createPlaylist("d1e5nqreqo", playListRequest);
    var playlist2 = playlistService.getPlaylist("2");
    assertEquals(3, playlist2.getMusics().size());
  }

  @Test
  @Order(50)
  void testGetPlaylistByEditor() throws IOException {
    var playlist = playlistService.getPlayListByEditor("d1e5nqreqo", 0, 5);
    assertEquals(2, playlist.size());
  }

  @Test
  @Order(60)
  void testGetPlaylistWithoutEditor() throws IOException {
    var playlist = playlistService.getPlayListByEditor(null, 0, 5);
    assertEquals(1, playlist.size());
  }

  @Test
  @Order(65)
  void testGetPlaylistWithoutEditorAndTitle() throws IOException {
    var playlist = playlistService.getPlayListByEditorAndTitle(null, null, 0, 5);
    assertEquals(1, playlist.size());
  }

  @Test
  @Order(70)
  void testGetPlaylistByEditorAndTitle() {
    var playlist = playlistService.getPlayListByEditorAndTitle("The Cradles", "d1e5nqreqo", 0, 5);
    assertEquals(2, playlist.size());
  }

  @Test
  @Order(80)
  void testGetPlaylistByEditorAndTitleWildCardTitle() {
    var playlist = playlistService.getPlayListByEditorAndTitle("C", "d1e5nqreqo", 0, 5);
    assertEquals(2, playlist.size());
  }
}
