package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.durex.music.entity.Music;
import com.github.durex.music.util.EntityMapper;
import com.github.durex.music.util.support.Data;
import com.github.durex.music.util.support.MockedMysql;
import com.github.durex.utils.Json;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = MockedMysql.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusicRepositoryTest {
  public static final String EDITOR = "ABCD001";
  public static final String MUSIC_ID = "111";
  public static final String DEVICE = "12345678";
  public static final String TITLE = "test2";
  public static final String THE_CRADLE = "The Cradle";
  static MockedMysql mysql = new MockedMysql();
  @Inject MusicRepository musicRepository;

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
    assertEquals(MUSIC_ID, music1.getId());
    assertEquals(THE_CRADLE, music1.getTitle());
  }

  @Test
  @Order(110)
  void testFindMusicReturnException() {
    assertThrows(NotFoundException.class, () -> musicRepository.findMusicById("123"));
  }

  @Test
  @Order(111)
  void testFindMusicByIdAndEditorReturnException() {
    assertThrows(
        NotFoundException.class, () -> musicRepository.findMusicsByIdAndEditor("123", EDITOR));
  }

  @Test
  @Order(112)
  void testFindMusicByIdAndEditorWhenEditorIsNull() {
    assertThrows(
        NotFoundException.class, () -> musicRepository.findMusicsByIdAndEditor("123", null));
  }

  @Test
  @Order(113)
  void testFindMusicByIdAndEditorWhenEditorIsEmpty() {
    assertThrows(NotFoundException.class, () -> musicRepository.findMusicsByIdAndEditor("123", ""));
  }

  @Test
  @Order(120)
  void testUpdateMusicById() {
    var music = musicRepository.findMusicById(MUSIC_ID);
    assertEquals(music.getId(), music.getId());
    music.setTitle(TITLE);
    musicRepository.updateMusic(music);
    var music1 = musicRepository.findMusicById(MUSIC_ID);
    assertEquals(TITLE, music1.getTitle());
  }

  @Test
  @Order(130)
  void testDeleteMusic() {
    musicRepository.deleteMusicById(MUSIC_ID);
    assertThrows(NotFoundException.class, () -> musicRepository.findMusicById(MUSIC_ID));
  }

  @Test
  @Order(140)
  void testInsertMusics() throws IOException {
    var music = Data.givenAMusic();
    var musicJpa = EntityMapper.musicJPARequestToEntity(music, EDITOR);
    for (int i = 0; i < 10; i++) {
      musicJpa.setId(String.valueOf(i+10));
      musicRepository.createMusic(musicJpa);
    }
    var musicsAll = musicRepository.findAllMusics();
    assertEquals(10, musicsAll.size());
    System.out.println(Json.toString(musicsAll));
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage(EDITOR, DEVICE, 0, 5);
    List<String> idLists = musics.stream().map(Music::getId).collect(Collectors.toList());
    assertEquals(5, musics.size());
    assertThat(idLists, hasItems("10", "11", "12", "13", "14"));
    var musics2 = musicRepository.findMusicByEditorAndDeviceWithPage(EDITOR, DEVICE, 1, 5);
    List<String> idLists2 = musics2.stream().map(Music::getId).collect(Collectors.toList());
    assertEquals(5, musics2.size());
    assertThat(idLists2, hasItems("15", "16", "17", "18", "19"));
  }

  @Test
  @Order(145)
  void testFindMusicByEditorAndDeviceWithPageWhenEditorIsNull() {
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage(null, DEVICE, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(146)
  void testFindMusicByEditorAndDeviceWithPageWhenEditorIsEmpty() {
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage("", DEVICE, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(147)
  void testFindMusicByEditorAndDeviceWithPageWhenDeviceIsEmpty() {
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage("", "", 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(148)
  void testFindMusicByEditorAndDeviceWithPageWhenDeviceIsNull() {
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage("", null, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(150)
  void testFindMusicByTitleAndEditor() {
    var musics = musicRepository.findMusicsByTitleAndEditorWithPage("Cra", EDITOR, 0, 5);
    assertEquals(5, musics.size());
  }

  @Test
  @Order(152)
  void testFindMusicByTitleAndEditorAndTitleIsEmpty() {
    var musics = musicRepository.findMusicsByTitleAndEditorWithPage("", EDITOR, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(153)
  void testFindMusicByTitleAndEditorAndTitleIsNull() {
    var musics = musicRepository.findMusicsByTitleAndEditorWithPage(null, EDITOR, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(154)
  void testFindMusicByTitleAndEditorWhenEditorIsEmpty() {
    var musics = musicRepository.findMusicsByTitleAndEditorWithPage(null, "", 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(155)
  void testFindMusicByTitleAndEditorWhenEditorIsNull() {
    var musics = musicRepository.findMusicsByTitleAndEditorWithPage(null, null, 0, 5);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(160)
  void testInsertMusicsWithoutEditor() throws IOException {
    var music = Data.givenAMusic();
    var musicJpa = EntityMapper.musicJPARequestToEntity(music, EDITOR);
    musicJpa.setId(MUSIC_ID + "1");
    musicJpa.setEditor(null);
    musicRepository.createMusic(musicJpa);
    var musicFromDB = musicRepository.findMusicById(MUSIC_ID + "1");
    assertEquals(MUSIC_ID + "1", musicFromDB.getId());
    assertEquals(THE_CRADLE, musicFromDB.getTitle());
    assertNull(musicFromDB.getEditor());
    var musics = musicRepository.findMusicByEditorAndDeviceWithPage(null, DEVICE, 0, 5);
    MatcherAssert.assertThat(musics, hasItems(musicFromDB));
  }

  @Test
  @Order(170)
  void testFindMusicsByIdAndEditor() throws IOException {
    var music = Data.givenAMusic();
    var musicJpa = EntityMapper.musicJPARequestToEntity(music, EDITOR);
    musicJpa.setId(MUSIC_ID + "1");
    musicJpa.setEditor(null);
    var musicFind = musicRepository.findMusicsByIdAndEditor(MUSIC_ID + "1", null);
    assertThat(musicFind, equalTo(musicJpa));
  }

  @Test
  @Order(180)
  void testInsertMusicsWithNullValue() throws IOException {
    var music = Data.givenAMusic();
    var musicJpa = EntityMapper.musicJPARequestToEntity(music, EDITOR);
    musicJpa.setId(MUSIC_ID + "2");
    musicJpa.setDevice(null);
    musicRepository.createMusic(musicJpa);
    assertThat(musicRepository.findMusicById(MUSIC_ID + "2"), is(musicJpa));
  }

  @Test
  @Order(182)
  void testFindMusicsByTitleAndDevice() {
    var musics = musicRepository.findMusicsByTitleAndDevice("Cra", DEVICE);
    assertEquals(11, musics.size());
  }

  @Test
  @Order(183)
  void testFindMusicsByTitleAndDeviceWithNullDevice() {
    var musics = musicRepository.findMusicsByTitleAndDevice("Cra", null);
    assertEquals(1, musics.size());
  }

  @Test
  @Order(185)
  void testFindMusicsByTitleAndDeviceWhenDeviceIsEmpty() {
    var musics = musicRepository.findMusicsByTitleAndDevice("Cra", "");
    assertEquals(1, musics.size());
  }

  @Test
  @Order(186)
  void testFindMusicsByTitleAndDeviceWithNullDeviceAndNullTitle() {
    var musics = musicRepository.findMusicsByTitleAndDevice(null, null);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(187)
  void testFindMusicsByTitleAndDeviceWithNullDeviceAndTitleIsEmtpy() {
    var musics = musicRepository.findMusicsByTitleAndDevice("", null);
    assertEquals(0, musics.size());
  }

  @Test
  @Order(190)
  void testFindMusicsByEditor() {
    var musics = musicRepository.findMusicsByEditor(EDITOR, 0, 5);
    assertEquals(5, musics.size());
  }

  @Test
  @Order(192)
  void testFindMusicsByNullEditor() {
    var musics = musicRepository.findMusicsByEditor(null, 0, 5);
    assertEquals(1, musics.size());
  }

  @Test
  @Order(193)
  void testFindMusicsByEditorWhenEditorIsEmpty() {
    var musics = musicRepository.findMusicsByEditor("", 0, 5);
    assertEquals(1, musics.size());
  }
}
