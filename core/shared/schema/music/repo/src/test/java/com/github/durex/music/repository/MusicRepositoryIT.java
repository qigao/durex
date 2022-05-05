package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.api.Music;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MusicRepositoryIT {
  public static final String RANDOM_TITLE = RandomStringUtils.randomAlphanumeric(15);
  public static final String ANOTHER_TITLE = RandomStringUtils.randomAlphanumeric(15);
  @Inject MusicRepository repository;

  @Test
  @Order(100)
  void testSaveMusicThenFindItThenDelete() {
    var musicId = UniqID.getId();
    var music = DemoMusicData.givenAMusic(musicId);
    Assertions.assertEquals(1, repository.save(music));
    assertTrue(repository.findById(musicId).isPresent());
    assertEquals(1, repository.deleteById(musicId));
  }

  @Test
  @Order(200)
  void testSaveMusicInBatchThenDelete() {
    var musicLists = DemoMusicData.givenSomeMusics(5);
    musicLists.forEach(m -> m.setTitle(RANDOM_TITLE + m.getTitle()));
    var resultLists = repository.save(musicLists);
    assertThat(resultLists, Matchers.not(0));
    var findable = repository.findByTitle(RANDOM_TITLE, WildCardType.CONTAINS);
    assertEquals(5, findable.size());
    musicLists.forEach(m -> m.setTitle(ANOTHER_TITLE));
    var batchUpdate = repository.update(musicLists);
    Assertions.assertEquals(5, batchUpdate.length);
    assertEquals(5, repository.deleteByTitle(ANOTHER_TITLE, WildCardType.CONTAINS));
    assertEquals(0, repository.findByTitle(ANOTHER_TITLE, WildCardType.CONTAINS).size());
  }

  @Test
  @Order(300)
  void testFindThenDeleteByTitle() {
    String musicId = UniqID.getId();
    var music = DemoMusicData.givenAMusic(musicId);
    music.setTitle(RANDOM_TITLE);
    Assertions.assertEquals(1, repository.save(music));
    var foundResult = repository.findByTitle(RANDOM_TITLE);
    var titleList = foundResult.stream().map(Music::getTitle).collect(Collectors.toList());
    assertEquals(1, titleList.size());
    assertEquals(1, repository.deleteByTitle(RANDOM_TITLE));
  }

  @Test
  @Order(400)
  void testUpdateThenDelete() {
    var music = DemoMusicData.givenAMusic(UniqID.getId());
    Assertions.assertEquals(1, repository.save(music));
    music.setTitle(RANDOM_TITLE);
    Assertions.assertEquals(1, repository.update(music));
    var foundResult = repository.findByTitle(RANDOM_TITLE);
    var titleList = foundResult.stream().map(Music::getTitle).collect(Collectors.toList());
    assertEquals(1, titleList.size());
    assertThat(repository.findAll().size(), Matchers.greaterThanOrEqualTo(1));
    assertNotEquals(
        0, repository.delete(foundResult.stream().map(Music::getId).collect(Collectors.toList())));
  }
}
