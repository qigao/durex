package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.github.durex.music.support.DemoMusicData;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uniqid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.test.StepVerifier;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MusicRepositoryIT {
  public static final String RANDOM_TITLE = RandomStringUtils.randomAlphanumeric(15);
  public static final String ANOTHER_TITLE = RandomStringUtils.randomAlphanumeric(15);
  @Inject MusicRepository repository;

  @Test
  @Order(100)
  @DisplayName("When save a music then update it's title")
  void testSaveMusicThenFindItThenDelete() {
    var musicId = UniqID.getId();
    var music = DemoMusicData.givenAMusic(musicId);
    assertAll(
        "save music",
        () -> repository.save(music).as(StepVerifier::create).expectNext(1).verifyComplete(),
        () ->
            repository
                .findById(musicId)
                .as(StepVerifier::create)
                .expectNext(music)
                .verifyComplete(),
        () -> music.setTitle(ANOTHER_TITLE),
        () -> repository.update(music).as(StepVerifier::create).expectNext(1).verifyComplete(),
        () ->
            repository
                .findById(musicId)
                .as(StepVerifier::create)
                .expectNext(music)
                .verifyComplete(),
        () ->
            repository
                .deleteById(musicId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());
  }

  @Test
  @Order(200)
  @DisplayName("When save and update music in batch")
  void testSaveMusicInBatchThenDelete() {
    var musicLists = DemoMusicData.givenSomeMusics(5);
    var newTitle = "new_title_for_batch_update";
    musicLists.forEach(m -> m.setTitle(newTitle + m.getTitle()));
    assertAll(
        "save musics in batch then update their title",
        () ->
            repository
                .save(musicLists)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .findByTitle(newTitle, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () -> musicLists.forEach(m -> m.setTitle(newTitle + ANOTHER_TITLE)),
        () ->
            repository
                .update(musicLists)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .deleteByTitle(newTitle, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNext(5)
                .verifyComplete(),
        () ->
            repository
                .findByTitle(newTitle, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete());
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
        () -> repository.save(music).as(StepVerifier::create).expectNextCount(1).verifyComplete(),
        () ->
            repository
                .findByTitle(RANDOM_TITLE)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            repository
                .deleteByTitle(RANDOM_TITLE)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());
  }
}
