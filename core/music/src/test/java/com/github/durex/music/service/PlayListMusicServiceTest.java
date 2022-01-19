package com.github.durex.music.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
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
class PlayListMusicServiceTest {
  public static final String EDITOR = "d1e5nqreqo";
  public static final String MUSIC_ID_6 = "wtVNDNP3YfqCOH7wKXStcEc61U6";
  public static final String MUSIC_ID_7 = "wtVNDNP3YfqCOH7wKXStcEc61U7";
  public static final String MUSIC_ID_8 = "wtVNDNP3YfqCOH7wKXStcEc61U8";
  public static final String MUSIC_ID_9 = "wtVNDNP3YfqCOH7wKXStcEc61U9";
  public static final String MUSIC_ID_1 = "wtVNDNP3YfqCOH7wKXStcEc61U1";
  public static final String MUSIC_ID_2 = "wtVNDNP3YfqCOH7wKXStcEc61U2";
  public static final String MUSIC_ID_3 = "wtVNDNP3YfqCOH7wKXStcEc61U3";
  public static final String MUSIC_ID_4 = "wtVNDNP3YfqCOH7wKXStcEc61U4";
  public static final String MUSIC_ID_5 = "wtVNDNP3YfqCOH7wKXStcEc61U5";
  static MockedMysql mysql = new MockedMysql();
  @Inject PlaylistService playlistService;
  @Inject MusicService musicService;
  @Inject PlayListMusicService playListMusicService;
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
  void testAddMusicToPlayListInBatch() throws IOException {
    // Given 5 musics
    Data.givenFiveMusics().forEach(music -> musicService.createMusic(EDITOR, music));
    // After playlist is created
    var playListRequest = Data.givenPlayListWithFiveMusics();
    playListRequest.setCoverUrl("http://localhost/music/demo.jpeg");
    UNIQUE_ID = playlistService.createPlaylist(EDITOR, playListRequest);
    playListMusicService.checkMusicIdThenAddToPlayList(
        UNIQUE_ID, List.of(MUSIC_ID_1, MUSIC_ID_2, MUSIC_ID_3, MUSIC_ID_4));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    assertThat(musicsInPlaylist.size(), is(4));
    assertEquals(4, playListMusicRepository.musicCountsOfPlayList(UNIQUE_ID));
  }

  @Test
  @Order(30)
  void testAddMusicToPlayListWithOrder() throws IOException {
    var musicAWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_6).order(5).build();
    var musicBWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_7).order(1).build();
    var musicCWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_8).order(3).build();
    var musicDWithOrder = OrderedMusicRequest.builder().musicId(MUSIC_ID_9).order(2).build();
    var musicRequest = Data.givenAMusic();
    musicRequest.setId(MUSIC_ID_6);
    musicService.createMusic(EDITOR, musicRequest);
    musicRequest.setId(MUSIC_ID_7);
    musicService.createMusic(EDITOR, musicRequest);
    musicRequest.setId(MUSIC_ID_8);
    musicService.createMusic(EDITOR, musicRequest);
    musicRequest.setId(MUSIC_ID_9);
    musicService.createMusic(EDITOR, musicRequest);
    playListMusicService.checkMusicIdThenAddToPlayListWithOrder(
        UNIQUE_ID, List.of(musicAWithOrder, musicBWithOrder, musicCWithOrder, musicDWithOrder));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    musicsInPlaylist.forEach(
        m -> {
          System.out.print(m.getMusic().getId());
          System.out.print("\t");
          System.out.println(m.getMusicOrder());
        });
    assertThat(musicsInPlaylist.size(), is(8));
    assertEquals(8, playListMusicRepository.musicCountsOfPlayList(UNIQUE_ID));
  }

  @Test
  @Order(40)
  void testDeleteMusicsFromPlayList() {
    playListMusicService.checkMusicIdThenDeleteFromPlayList(
        UNIQUE_ID, List.of(MUSIC_ID_1, MUSIC_ID_2, MUSIC_ID_3, MUSIC_ID_4));
    var playlist = playlistService.findPlayListById(UNIQUE_ID);
    var musicsInPlaylist = playlist.getPlayListAssociations();
    assertThat(musicsInPlaylist.size(), is(4));
    assertEquals(4, playListMusicRepository.musicCountsOfPlayList(UNIQUE_ID));
  }
}
