package com.github.durex.basic.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.model.PlayListRequest;
import com.github.durex.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
class PlaylistServiceTest {
  public static final String JSON_FILE = "src/test/resources/json/playlist/demo.json";
  static MockedMysql mysql = new MockedMysql();
  @Inject PlaylistService playlistService;
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
  void createPlaylist() throws IOException {
    var jsonFile = JSON_FILE;
    var requestJson = Json.read(Paths.get(jsonFile).toFile(), PlayListRequest.class);
    var playlist = playlistService.createPlaylist(null, requestJson);
    assertEquals("1", playlist.getId());
  }

  @Test
  @Order(15)
  void testCreatePlaylistWithSameName() throws IOException {
    var jsonFile = JSON_FILE;
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

  @Test
  @Order(90)
  void testCreatePlaylistThenUpdate() throws IOException {
    var musicList = "src/test/resources/json/music/music5.json";
    var playList = "src/test/resources/json/playlist/real.json";
    var musicListRequest =
        Json.read(Paths.get(musicList).toFile(), new TypeReference<List<MusicRequest>>() {});
    musicListRequest.forEach(music -> musicServiceService.createMusic("d1e5nqreqo", music));
    var playListRequest = Json.read(Paths.get(playList).toFile(), PlayListRequest.class);
    playListRequest.setCoverUrl("http://localhost/music/demo.jpeg");
    var playlist = playlistService.createPlaylist("d1e5nqreqo", playListRequest);
    var playlist2 = playlistService.getPlaylist("1tVNDNP3YfqCOH7wKXStcEc61UP");
    assertEquals(5, playlist2.getMusics().size());
  }
}
