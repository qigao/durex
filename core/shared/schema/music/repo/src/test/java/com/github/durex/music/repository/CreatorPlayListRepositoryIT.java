package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@Slf4j
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreatorPlayListRepositoryIT {
  @Inject PlayListMusicRepository playListMusicRepository;
  @Inject MusicRepository musicRepository;
  @Inject PlayListRepository playListRepository;

  @Inject CreatorPlayListRepository creatorPlayListRepository;

  @Test
  @Order(100)
  void testSavePlayListToCreator() {
    var musics = DemoMusicData.givenSomeMusics(20);
    var result = musicRepository.save(musics);
    Assertions.assertEquals(20, result.length);
    var playList = DemoMusicData.givenAPlayList();
    var savedPlaylist = playListRepository.save(playList);
    Assertions.assertEquals(1, savedPlaylist);
    var playlistId = playList.getId();
    var saved = playListMusicRepository.saveMusicsToPlayList(playlistId, musics);
    assertEquals(20, saved.length);
    var playlists = playListMusicRepository.listMusicsByPlayListId(playlistId);
    assertEquals(20, playlists.size());
    var creatorId = UniqID.getId();
    Assertions.assertEquals(
        1, creatorPlayListRepository.savePlaylistToCreator(creatorId, playlistId));
    assertEquals(1, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size());
    assertEquals(1, creatorPlayListRepository.deletePlaylistFromCreator(creatorId, playlistId));
    assertEquals(20, playListMusicRepository.clearMusicsFromPlayList(playlistId));
    assertEquals(0, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size());
    Assertions.assertEquals(1, playListRepository.deleteById(playlistId));
  }

  @Test
  @Order(200)
  void testSavePlayListToCreatorWithSamePlaylist() {
    // given 20 musics
    var musics = DemoMusicData.givenSomeMusics(20);
    var result = musicRepository.save(musics);
    Assertions.assertEquals(20, result.length);
    // given playlist with 20 musics
    var playlists = DemoMusicData.givenSomePlayList(20);
    var savedPlaylists = playListRepository.save(playlists);
    Assertions.assertEquals(20, savedPlaylists.length);
    // given playlistId 20 times
    var playlistIds = playlists.parallelStream().map(PlayList::getId).collect(Collectors.toList());

    // then save music to playlist
    playlistIds.forEach(
        id -> {
          var saved = playListMusicRepository.saveMusicsToPlayList(id, musics);
          assertEquals(20, saved.length);
        });

    // then save playlist to creator
    var creatorId = UniqID.getId();
    var savedResult = creatorPlayListRepository.savePlaylistToCreator(creatorId, playlistIds);
    assertThat(savedResult, Matchers.not(0));
    assertEquals(20, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size());
    // then delete playlist from creator
    var deleted = creatorPlayListRepository.deletePlaylistsFromCreator(creatorId, playlistIds);
    assertEquals(20, deleted.length);
    // then delete playlist
    var deletedPlaylist = playListRepository.deleteById(playlistIds);
    assertEquals(20, deletedPlaylist);
    // then delete music
    var deletedMusic =
        musicRepository.delete(
            musics.parallelStream().map(Music::getId).collect(Collectors.toList()));
    assertEquals(20, deletedMusic);
  }
}
