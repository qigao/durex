package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.api.PlayList;
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
class PlayListRepositoryIT {
  public static final String ANOTHER_TITLE = RandomStringUtils.randomAlphanumeric(5);
  @Inject PlayListRepository repository;
  public static final String RANDOM_TITLE = RandomStringUtils.randomAlphanumeric(5);

  @Test
  @Order(100)
  void testSavePlayListThenFindByIdThenDelete() {
    var playlist = DemoMusicData.givenAPlayList();
    var playlistId = playlist.getId();
    Assertions.assertEquals(1, repository.save(playlist));
    var savedPlaylist = repository.findById(playlistId);
    assertTrue(savedPlaylist.isPresent());
    Assertions.assertEquals(1, repository.deleteById(playlistId));
  }

  @Test
  @Order(200)
  void testSavePlayListInBatchThenDelete() {
    var playlists = DemoMusicData.givenSomePlayList(5);
    playlists.forEach(p -> p.setTitle(RANDOM_TITLE + p.getTitle()));
    var results = repository.save(playlists);
    assertThat(results, Matchers.not(0));
    var findall = repository.findByTitle(RANDOM_TITLE, WildCardType.CONTAINS);
    assertThat(findall.size(), Matchers.is(5));
    playlists.forEach(p -> p.setTitle(ANOTHER_TITLE));
    var batchUpdate = repository.update(playlists);
    Assertions.assertEquals(5, batchUpdate.length);
    assertThat(repository.findAll().size(), Matchers.greaterThanOrEqualTo(5));
    assertEquals(5, repository.deleteByTitle(ANOTHER_TITLE, WildCardType.CONTAINS));
  }

  @Test
  @Order(300)
  void testFindThenDeleteByTitle() {
    var playlistId = UniqID.getId();
    var playlist = DemoMusicData.givenAPlayList(playlistId);
    playlist.setTitle(RANDOM_TITLE);
    Assertions.assertEquals(1, repository.save(playlist));
    var found = repository.findByTitle(RANDOM_TITLE);
    var titleList = found.stream().map(PlayList::getTitle).collect(Collectors.toList());
    assertEquals(1, titleList.size());
    assertEquals(1, repository.deleteByTitle(RANDOM_TITLE));
  }

  @Test
  @Order(400)
  void testUpdateThenDelete() {
    var playlist = DemoMusicData.givenAPlayList();
    Assertions.assertEquals(1, repository.save(playlist));
    playlist.setTitle(RANDOM_TITLE);
    Assertions.assertEquals(1, repository.update(playlist));
    var found = repository.findByTitle(RANDOM_TITLE);
    var titleList = found.stream().map(PlayList::getTitle).collect(Collectors.toList());
    assertEquals(1, titleList.size());
    assertNotEquals(
        0, repository.deleteById(found.stream().map(PlayList::getId).collect(Collectors.toList())));
  }
}
