package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.durex.music.model.PlayList;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.sqlbuilder.enums.WildCardType;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

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
        () -> assertEquals(1, repository.save(playlist)),
        () -> assertTrue(repository.findById(playlistId).isPresent()),
        () -> assertEquals(1, repository.deleteById(playlistId)));
  }

  @Test
  @Order(200)
  @DisplayName("When find playlist by title with wildcard")
  void testSavePlayListInBatchThenDelete() {
    var playlists = DemoMusicData.givenSomePlayList(5);
    playlists.forEach(p -> p.setTitle(RANDOM_TITLE + p.getTitle()));
    var results = repository.save(playlists);
    assertThat(results, Matchers.not(0));
    assertThat(repository.findByTitle(RANDOM_TITLE, WildCardType.CONTAINS).size(), Matchers.is(5));
    playlists.forEach(p -> p.setTitle(ANOTHER_TITLE));
    assertAll(
        "update playlist",
        () -> assertEquals(5, repository.update(playlists).length),
        () -> assertThat(repository.findAll().size(), Matchers.greaterThanOrEqualTo(5)),
        () -> assertEquals(5, repository.deleteByTitle(ANOTHER_TITLE, WildCardType.CONTAINS)));
  }

  @Test
  @Order(300)
  @DisplayName("When find playlist by title")
  void testFindThenDeleteByTitle() {
    var playlistId = UniqID.getId();
    var playlist = DemoMusicData.givenAPlayList(playlistId);
    playlist.setTitle(RANDOM_TITLE);
    assertAll(
        "save playlist then find by title",
        () -> assertEquals(1, repository.save(playlist)),
        () -> assertEquals(1, repository.findByTitle(RANDOM_TITLE).size()),
        () -> assertEquals(1, repository.deleteByTitle(RANDOM_TITLE)));
  }

  @Test
  @Order(400)
  @DisplayName("When update playlist title")
  void testUpdateThenDelete() {
    var playlist = DemoMusicData.givenAPlayList();
    assertAll(
        "update playlist title",
        () -> assertEquals(1, repository.save(playlist)),
        () -> playlist.setTitle(RANDOM_TITLE),
        () -> assertEquals(1, repository.update(playlist)));
    List<PlayList> playLists = repository.findByTitle(RANDOM_TITLE);
    List<String> playlistIds = playLists.stream().map(PlayList::getId).collect(Collectors.toList());
    assertAll(
        "check playlist",
        () -> assertEquals(1, playLists.size()),
        () -> assertThat(repository.deleteById(playlistIds), Matchers.greaterThan(0)));
  }
}
