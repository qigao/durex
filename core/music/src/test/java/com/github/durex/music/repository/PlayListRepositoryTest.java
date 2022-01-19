package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.durex.music.util.EntityMapper;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
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
@QuarkusTestResource(value = MockedMysql.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayListRepositoryTest {
  public static final String ID1 = "1";
  public static final String ID2 = "2";
  public static final String ID3 = "3";
  public static final String TITLE = "Cra";
  public static final String DEVICE = "ABCD";
  static MockedMysql mysql = new MockedMysql();
  @Inject PlayListRepository playListRepository;
  @Inject MusicRepository musicRepository;
  @Inject PlayListMusicRepository playListMusicRepository;
  public static final String EDITOR = "ABCD001";
  public static final String MUSIC_ID = "111";

  @BeforeAll
  static void setUp() {
    mysql.start();
  }

  @AfterAll
  static void tearDown() {
    mysql.stop();
  }

  @Test
  @Order(100)
  void testCreateMusic() throws IOException {
    var music = Data.givenAMusic();
    var musicJpa = EntityMapper.musicJPARequestToEntity(music, EDITOR);
    musicJpa.setId(MUSIC_ID);
    musicRepository.createMusic(musicJpa);
    var music1 = musicRepository.findMusicById(MUSIC_ID);
    Assertions.assertEquals(MUSIC_ID, music1.getId());
    Assertions.assertEquals("The Cradle", music1.getTitle());
  }

  @Test
  @Order(110)
  void testCreatePlayList() throws IOException {
    var playList = Data.givenPlayListWithInitialMusics();
    var entity = EntityMapper.playListRequestToJPAEntity(playList, EDITOR);
    playListRepository.createPlayList(entity);
    assertEquals(playList.getId(), playListRepository.findPlayList(playList.getId()).getId());
    playList.setId(ID2);
    var entityWithoutEditor = EntityMapper.playListRequestToJPAEntity(playList, null);
    playListRepository.createPlayList(entityWithoutEditor);
  }

  @Test
  @Order(120)
  void testFindPlayListByIdAndEditor() {
    var playList = playListRepository.findPlayListByIdAndEditor(ID1, EDITOR);
    assertEquals(ID1, playList.get(0).getId());
  }

  @Test
  @Order(130)
  void testFindPlayListByIdAndEditorIsNull() {
    var playList = playListRepository.findPlayListByIdAndEditor(ID2, null);
    assertEquals(ID2, playList.get(0).getId());
  }

  @Test
  @Order(130)
  void testFindPlayListByIdAndEditorIsEmpty() {
    var playList = playListRepository.findPlayListByIdAndEditor(ID2, "");
    assertEquals(ID2, playList.get(0).getId());
  }

  @Test
  @Order(160)
  void testFindPlayListByEditorAndTitle() {
    var playList = playListRepository.findPlayListByEditorAndTitleWithPage(TITLE, EDITOR, 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(161)
  void testFindPlayListByEditorAndTitleWhenEditorIsNull() {
    var playList = playListRepository.findPlayListByEditorAndTitleWithPage(TITLE, null, 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(162)
  void testFindPlayListByEditorAndTitleWheEditorIsEmpty() {
    var playList = playListRepository.findPlayListByEditorAndTitleWithPage(TITLE, "", 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(163)
  void testFindPlayListByEditorAndTitleWheTitleIsEmpty() {
    var playList = playListRepository.findPlayListByEditorAndTitleWithPage("", "", 0, 5);
    assertEquals(0, playList.size());
  }

  @Test
  @Order(163)
  void testFindPlayListByEditorAndTitleWheTitleIsNull() {
    var playList = playListRepository.findPlayListByEditorAndTitleWithPage(null, "", 0, 5);
    assertEquals(0, playList.size());
  }

  @Test
  @Order(170)
  void testFindPlayListByEditorAndDeviceWithPage() {
    var playList = playListRepository.findPlayListByEditorAndDeviceWithPage(EDITOR, DEVICE, 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(171)
  void testFindPlayListByEditorAndDeviceWithPageWhenEditorIsNull() {
    var playList = playListRepository.findPlayListByEditorAndDeviceWithPage(null, DEVICE, 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(171)
  void testFindPlayListByEditorAndDeviceWithPageWhenEditorIsEmpty() {
    var playList = playListRepository.findPlayListByEditorAndDeviceWithPage("", DEVICE, 0, 5);
    assertEquals(1, playList.size());
  }

  @Test
  @Order(171)
  void testFindPlayListByEditorAndDeviceWithPageWhenDeviceIsNull() {
    var playList = playListRepository.findPlayListByEditorAndDeviceWithPage(null, null, 0, 5);
    assertEquals(0, playList.size());
  }

  @Test
  @Order(171)
  void testFindPlayListByEditorAndDeviceWithPageWhenDeviceIsEmpty() {
    var playList = playListRepository.findPlayListByEditorAndDeviceWithPage("", "", 0, 5);
    assertEquals(0, playList.size());
  }

  @Test
  @Order(180)
  void testUpdatePlayList() {
    var result = playListRepository.findPlayList(ID1);
    result.setTitle("Demo");
    playListRepository.updatePlayList(result);
    assertEquals("Demo", playListRepository.findPlayList(ID1).getTitle());
  }

  @Test
  @Order(200)
  void testDeletePlayList() {
    var result = playListRepository.deletePlayListById(ID1);
    assertEquals(1, result);
    assertThrows(NotFoundException.class, () -> playListRepository.findPlayList(ID1));
  }

  @Test
  @Order(210)
  void testDeletePlayListByIdAndEditorWhenEditorIsNull() {
    var result = playListRepository.deletePlayListByIdAndEditor(ID2, null);
    assertEquals(1, result);
    assertThrows(NotFoundException.class, () -> playListRepository.findPlayList(ID2));
    var musics = playListMusicRepository.getMusicIdsByPlayListId(ID2);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(215)
  void testCreatePlayListWithID3() throws IOException {
    var playList = Data.givenPlayListWithInitialMusics();
    playList.setId(ID3);
    var entity = EntityMapper.playListRequestToJPAEntity(playList, null);
    playListRepository.createPlayList(entity);
    assertEquals(ID3, playListRepository.findPlayList(playList.getId()).getId());
  }

  @Test
  @Order(220)
  void testDeletePlayListByIdAndEditorWhenEditorIsEmpty() {
    var result = playListRepository.deletePlayListByIdAndEditor(ID3, "");
    assertEquals(1, result);
    assertThrows(NotFoundException.class, () -> playListRepository.findPlayList(ID3));
    var musics = playListMusicRepository.getMusicIdsByPlayListId(ID3);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(225)
  void testCreatePlayListWithID3WithEditorName() throws IOException {
    var playList = Data.givenPlayListWithInitialMusics();
    playList.setId(ID3);
    var entity = EntityMapper.playListRequestToJPAEntity(playList, EDITOR);
    playListRepository.createPlayList(entity);
    assertEquals(ID3, playListRepository.findPlayList(playList.getId()).getId());
  }

  @Test
  @Order(230)
  void testDeletePlayListByIdAndEditorWhenEditorIsNotNull() {
    var result = playListRepository.deletePlayListByIdAndEditor(ID3, EDITOR);
    assertEquals(1, result);
    assertThrows(NotFoundException.class, () -> playListRepository.findPlayList(ID3));
    var musics = playListMusicRepository.getMusicIdsByPlayListId(ID3);
    assertEquals(0, musics.size());
  }
}
