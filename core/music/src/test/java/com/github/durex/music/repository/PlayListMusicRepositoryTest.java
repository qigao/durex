package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
class PlayListMusicRepositoryTest {
  public static final String EDITOR = "d1e5nqreqo";
  public static final String MUSIC_ID_1 = "wtVNDNP3YfqCOH7wKXStcEc61U1";
  public static final String MUSIC_ID_2 = "wtVNDNP3YfqCOH7wKXStcEc61U2";
  public static final String MUSIC_ID_3 = "wtVNDNP3YfqCOH7wKXStcEc61U3";
  public static final String MUSIC_ID_4 = "wtVNDNP3YfqCOH7wKXStcEc61U4";
  public static final String MUSIC_ID_5 = "wtVNDNP3YfqCOH7wKXStcEc61U5";
  static MockedMysql mysql = new MockedMysql();
  @Inject PlaylistService playlistService;
  @Inject MusicService musicService;
  @Inject PlayListMusicRepository playListMusicRepository;
  private String UNIQUE_ID = "";

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
  void createMusicAndPlayList() throws IOException {
    // Given 5 musics
    Data.givenFiveMusics().forEach(music -> musicService.createMusic(EDITOR, music));
    // After playlist is created
    var playListRequest = Data.givenPlayListWithFiveMusics();
    playListRequest.setCoverUrl("http://localhost/music/demo.jpeg");
    UNIQUE_ID = playlistService.createPlaylist(EDITOR, playListRequest);
    // Then check the playlist
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    // Lastly
    assertEquals(playlist.getId(), UNIQUE_ID);
  }

  @Test
  @Order(20)
  void addMusicToPlayList() throws IOException {
    var music = Data.givenAMusic();
    musicService.createMusic(EDITOR, music);
    assertEquals(1, playListMusicRepository.addMusicToPlayList(UNIQUE_ID, "6", 0));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    assertEquals(UNIQUE_ID, playlist.getId());
    var playlistMusic = playlist.getPlayListAssociations();
    assertEquals(1, playlistMusic.size());
    assertThat(playlistMusic.get(0).getMusic(), hasProperty("id", is("6")));
  }

  @Test
  @Order(22)
  void getPlayListMusicByPlayListAndMusicIDs() {
    var musicInPlayList = playListMusicRepository.musicIdInPlayList(UNIQUE_ID, "6");
    Assertions.assertTrue(musicInPlayList);
  }

  @Test
  @Order(23)
  void getPlayListMusicByPlayListAndMusicIDsInBatch() {
    var musicInPlayList =
        playListMusicRepository.checkMusicAlreadyExistsInPlayList(
            UNIQUE_ID, List.of("6", "7", "8"));
    Assertions.assertTrue(musicInPlayList > 0);
  }

  @Test
  @Order(25)
  void deletePlayListMusic() {
    playListMusicRepository.deleteMusicFromPlayList(UNIQUE_ID, "6");
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    System.out.println(playlist.getPlayListAssociations());
    assertThat(playlist.getPlayListAssociations().size(), is(0));
  }

  @Test
  @Order(26)
  void testCheckIfMusicNotExits() {
    assertNotEquals(3, playListMusicRepository.checkIfMusicNotExists(List.of("6", "7", "8")));
  }

  @Test
  @Order(30)
  void addMusicToPlayListInBatch() {
    playListMusicRepository.addMusicToPlayListInBatch(
        UNIQUE_ID, List.of(MUSIC_ID_1, MUSIC_ID_2, MUSIC_ID_3, MUSIC_ID_4));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    assertThat(musicsInPlaylist.size(), is(4));
    assertEquals(4, playListMusicRepository.musicCountsOfPlayList(UNIQUE_ID));
  }

  @Test
  @Order(36)
  void getMusicsFromPlayList() {
    var playlistId = playListMusicRepository.getMusicIdsByPlayListId(UNIQUE_ID);
    assertThat(playlistId, hasItems(MUSIC_ID_1, MUSIC_ID_2, MUSIC_ID_3, MUSIC_ID_4));
  }

  @Test
  @Order(40)
  void addMusicToPlayListWithOrderInBatch() {
    assertEquals(1, playListMusicRepository.addMusicToPlayList(UNIQUE_ID, MUSIC_ID_5, 1));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    var musicOrders =
        musicsInPlaylist.stream().filter(p -> p.getMusicOrder() == 1).collect(Collectors.toList());
    assertThat(musicOrders, hasItem(hasProperty("music", hasProperty("id", is(MUSIC_ID_5)))));
  }

  @Test
  @Order(50)
  void testDeleteMusicFromPlayListInBatch() {
    playListMusicRepository.deleteMusicFromPlayListInBatch(
        UNIQUE_ID, List.of(MUSIC_ID_3, MUSIC_ID_4));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    assertThat(musicsInPlaylist.size(), is(3));
    assertEquals(3, playListMusicRepository.musicCountsOfPlayList(UNIQUE_ID));
    var musicInPlayList = playListMusicRepository.musicIdInPlayList(UNIQUE_ID, MUSIC_ID_3);
    Assertions.assertFalse(musicInPlayList);
  }

  @Test
  @Order(60)
  void testEmptyPlayList() {
    playListMusicRepository.emptyPlayList(UNIQUE_ID);
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    assertTrue(playlist.getPlayListAssociations().isEmpty());
  }
}
