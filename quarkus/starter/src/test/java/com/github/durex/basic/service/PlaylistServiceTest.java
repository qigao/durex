package com.github.durex.basic.service;

import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.MysqlResources;
import com.github.durex.basic.exception.PlayListNotFoundException;
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
  void createPlaylist() throws PlayListNotFoundException, IOException {
    var jsonFile = "src/test/resources/json/playlist.json";
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    var playlist = playlistService.createPlaylist(null, requestJson);
    assert (playlist.getId() == 1);
  }

  @Test
  @Order(15)
  void testCreatePlaylistWithSameName() throws IOException, PlayListNotFoundException {
    var jsonFile = "src/test/resources/json/playlist.json";
    var playListRequest = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    playListRequest.setId(2L);
    var playlist = playlistService.createPlaylist("d1e5nqreqo", playListRequest);
    var playlist2 = playlistService.getPlaylist(2L);
    assert (playlist2.getMusics().size() == 3);
  }

  @Test
  @Order(30)
  void testGetPlaylistNotFound() {
    assertThrows(PlayListNotFoundException.class, () -> playlistService.getPlaylist(100L));
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
