package com.github.durex.music.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayList;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

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
  @DisplayName("When save playlist to creator")
  void testSavePlayListToCreator() {
    var musics = DemoMusicData.givenSomeMusics(20);
    var result = musicRepository.save(musics);
    assertEquals(20, result.length);
    var playList = DemoMusicData.givenAPlayList();
    var savedPlaylist = playListRepository.save(playList);
    assertEquals(1, savedPlaylist);
    var playlistId = playList.getId();

    assertAll(
        "save musics to a playlist",
        () ->
            assertEquals(
                20, playListMusicRepository.saveMusicsToPlayList(playlistId, musics).length),
        () ->
            assertThat(
                playListMusicRepository.listMusicsByPlayListId(playlistId), Matchers.hasSize(20)));
    var creatorId = UniqID.getId();
    assertAll(
        "save playlist to creator",
        () ->
            assertEquals(1, creatorPlayListRepository.savePlaylistToCreator(creatorId, playlistId)),
        () -> assertEquals(1, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () ->
            assertEquals(
                1, creatorPlayListRepository.deletePlaylistFromCreator(creatorId, playlistId)),
        () -> assertEquals(20, playListMusicRepository.clearMusicsFromPlayList(playlistId)),
        () -> assertEquals(0, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () -> assertEquals(1, playListRepository.deleteById(playlistId)));
  }

  @Test
  @Order(200)
  @DisplayName("When save multiple playlists to creator")
  void testSavePlayListToCreatorWithSamePlaylist() {
    // given 20 musics
    var musics = DemoMusicData.givenSomeMusics(20);
    List<String> musicIds = musics.parallelStream().map(Music::getId).collect(Collectors.toList());
    var result = musicRepository.save(musics);
    assertEquals(20, result.length);
    // given playlist with 20 musics
    var playlists = DemoMusicData.givenSomePlayList(20);
    assertEquals(20, playListRepository.save(playlists).length);
    // given playlistId 20 times
    var playlistIds = playlists.parallelStream().map(PlayList::getId).collect(Collectors.toList());

    // then save music to playlist
    playlistIds.forEach(
        id -> {
          assertEquals(20, playListMusicRepository.saveMusicsToPlayList(id, musics).length);
        });

    // then save playlist to creator
    var creatorId = UniqID.getId();
    assertAll(
        "save playlist to creator",
        () ->
            assertThat(
                creatorPlayListRepository.savePlaylistToCreator(creatorId, playlistIds),
                Matchers.not(0)),
        () ->
            assertEquals(20, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () ->
            // then delete playlist from creator
            assertEquals(
                20,
                creatorPlayListRepository.deletePlaylistsFromCreator(creatorId, playlistIds)
                    .length),
        // then delete playlist
        () -> assertEquals(20, playListRepository.deleteById(playlistIds)),
        // then delete music
        () -> assertEquals(20, musicRepository.delete(musicIds)));
  }
}
