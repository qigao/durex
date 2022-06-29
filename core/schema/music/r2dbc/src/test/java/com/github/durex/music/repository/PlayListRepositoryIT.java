package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.music.model.PlayList;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uniqid.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayListRepositoryIT {
  public static final String ANOTHER_TITLE = RandomStringUtils.randomAlphanumeric(5);
  @Inject PlayListRepository repository;
  public static final String RANDOM_TITLE = RandomStringUtils.randomAlphanumeric(5);

  @Test
  @Order(100)
  @DisplayName("When find playlist by id")
  void testSavePlayListThenFindByIdThenDelete() {
    var playlist = DemoMusicData.givenAPlayList();
    var playlistId = playlist.getId();
    assertAll(
        "save playlist",
        () ->
            repository.save(playlist).as(StepVerifier::create).expectNextCount(1).verifyComplete(),
        () ->
            repository
                .findById(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () -> playlist.setTitle(ANOTHER_TITLE),
        () ->
            repository
                .update(playlist)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            repository
                .deleteById(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());
  }

  @Test
  @Order(200)
  @DisplayName("When find playlist by title with wildcard")
  void testSavePlayListInBatchThenDelete() {
    var playlists = DemoMusicData.givenSomePlayList(5);
    playlists.forEach(p -> p.setTitle(RANDOM_TITLE + p.getTitle()));
    assertAll(
        "save playlist",
        () ->
            repository.save(playlists).as(StepVerifier::create).expectNextCount(5).verifyComplete(),
        () ->
            repository
                .findByTitle(RANDOM_TITLE, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete());
    var new_title = "new_title_query_batch";
    assertAll(
        "update playlist",
        () ->
            playlists.forEach(p -> p.setTitle(new_title + RandomStringUtils.randomAlphanumeric(5))),
        () ->
            repository
                .update(playlists)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .findByTitle(new_title, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .deleteByTitle(new_title, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNext(5)
                .verifyComplete());
  }

  @Test
  @Order(300)
  @DisplayName("When find playlist by title")
  void testFindThenDeleteByTitle() {
    var playlistId = UniqID.getId();
    var playlist = DemoMusicData.givenAPlayList(playlistId);
    var new_title = "new_title_query_batch";
    playlist.setTitle(new_title);
    assertAll(
        "save playlist then find by title",
        () ->
            repository.save(playlist).as(StepVerifier::create).expectNextCount(1).verifyComplete(),
        () ->
            repository
                .findByTitle(new_title)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            repository
                .deleteByTitle(new_title)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());
  }

  @Test
  @Order(400)
  @DisplayName("When find playlist by some ids")
  void testFindPlayListInBatchThenDelete() {
    var playlists = DemoMusicData.givenSomePlayList(5);
    playlists.forEach(p -> p.setTitle(RANDOM_TITLE + p.getTitle()));
    assertAll(
        "save playlist",
        () ->
            repository.save(playlists).as(StepVerifier::create).expectNextCount(5).verifyComplete(),
        () ->
            repository
                .findByTitle(RANDOM_TITLE, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete());
    var new_title = "new_title_query_batch";
    playlists.forEach(p -> p.setTitle(new_title + RandomStringUtils.randomAlphanumeric(5)));
    var playlistIds = playlists.stream().map(PlayList::getId).collect(Collectors.toList());
    assertAll(
        "update playlist",
        () ->
            repository
                .update(playlists)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .findByTitle(new_title, WildCardType.CONTAINS)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete(),
        () ->
            repository
                .deleteById(playlistIds)
                .as(StepVerifier::create)
                .expectNext(5)
                .verifyComplete());
  }
}
