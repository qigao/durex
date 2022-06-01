package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.music.model.Music;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MusicRepositoryIT {
  public static final String RANDOM_TITLE = RandomStringUtils.randomAlphanumeric(15);
  public static final String ANOTHER_TITLE = RandomStringUtils.randomAlphanumeric(15);
  @Inject MusicRepository repository;

  @Test
  @Order(100)
  @DisplayName("When save a music")
  void testSaveMusicThenFindItThenDelete() {
    var musicId = UniqID.getId();
    var music = DemoMusicData.givenAMusic(musicId);
    assertAll(
        "save music",
        () -> assertEquals(1, repository.save(music)),
        () -> assertTrue(repository.findById(musicId).isPresent()),
        () -> assertEquals(1, repository.deleteById(musicId)));
  }

  @Test
  @Order(200)
  @DisplayName("When update music title")
  void testSaveMusicInBatchThenDelete() {
    var musicLists = DemoMusicData.givenSomeMusics(5);
    musicLists.forEach(m -> m.setTitle(RANDOM_TITLE + m.getTitle()));
    assertAll(
        "save musics in batch then update their title",
        () -> assertThat(repository.save(musicLists), Matchers.not(0)),
        () -> assertEquals(5, repository.findByTitle(RANDOM_TITLE, WildCardType.CONTAINS).size()),
        () -> musicLists.forEach(m -> m.setTitle(ANOTHER_TITLE)),
        () -> assertEquals(5, repository.update(musicLists).length),
        () -> assertEquals(5, repository.deleteByTitle(ANOTHER_TITLE, WildCardType.CONTAINS)),
        () -> assertEquals(0, repository.findByTitle(ANOTHER_TITLE, WildCardType.CONTAINS).size()));
  }

  @Test
  @Order(300)
  @DisplayName("When delete musics by title")
  void testFindThenDeleteByTitle() {
    String musicId = UniqID.getId();
    var music = DemoMusicData.givenAMusic(musicId);
    music.setTitle(RANDOM_TITLE);
    assertAll(
        "save music then find it by title",
        () -> assertEquals(1, repository.save(music)),
        () -> assertEquals(1, repository.findByTitle(RANDOM_TITLE).size()),
        () -> assertEquals(1, repository.deleteByTitle(RANDOM_TITLE)));
  }

  @Test
  @Order(400)
  @DisplayName("When update music title")
  void testUpdateThenDelete() {
    var music = DemoMusicData.givenAMusic(UniqID.getId());
    Assertions.assertEquals(1, repository.save(music));
    music.setTitle(RANDOM_TITLE);
    Assertions.assertEquals(1, repository.update(music));
    var foundResult = repository.findByTitle(RANDOM_TITLE);
    var musicIds = foundResult.stream().map(Music::getId).collect(Collectors.toList());

    assertAll(
        "update then delete",
        () -> assertEquals(1, foundResult.size()),
        () -> assertThat(repository.findAll().size(), Matchers.greaterThanOrEqualTo(1)),
        () -> assertThat(repository.delete(musicIds), Matchers.greaterThan(0)));
  }
}
