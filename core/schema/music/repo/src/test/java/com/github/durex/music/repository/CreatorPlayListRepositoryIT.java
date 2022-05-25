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
    var musics = DemoMusicData.givenSomeMusics(5);
    var result = musicRepository.save(musics);
    assertEquals(5, result.length);
    var playList = DemoMusicData.givenAPlayList();
    var savedPlaylist = playListRepository.save(playList);
    assertEquals(1, savedPlaylist);
    var playlistId = playList.getId();

    assertAll(
        "save musics to a playlist",
        () ->
            assertEquals(
                5, playListMusicRepository.saveMusicsToPlayList(playlistId, musics).length),
        () ->
            assertThat(
                playListMusicRepository.listMusicsByPlayListId(playlistId), Matchers.hasSize(5)));
    var creatorId = UniqID.getId();
    assertAll(
        "save playlist to creator",
        () ->
            assertEquals(1, creatorPlayListRepository.savePlaylistToCreator(creatorId, playlistId)),
        () -> assertEquals(1, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () ->
            assertEquals(
                1, creatorPlayListRepository.deletePlaylistFromCreator(creatorId, playlistId)),
        () -> assertEquals(5, playListMusicRepository.clearMusicsFromPlayList(playlistId)),
        () -> assertEquals(0, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () -> assertEquals(1, playListRepository.deleteById(playlistId)));
  }

  @Test
  @Order(200)
  @DisplayName("When save multiple playlists to creator")
  void testSavePlayListToCreatorWithSamePlaylist() {
    // given  musics
    int num = 3;
    var musics = DemoMusicData.givenSomeMusics(num);
    List<String> musicIds = musics.parallelStream().map(Music::getId).collect(Collectors.toList());
    var result = musicRepository.save(musics);
    assertEquals(num, result.length);
    // given playlist with 20 musics
    int playlistNum = 2;
    var playlists = DemoMusicData.givenSomePlayList(playlistNum);
    assertEquals(playlistNum, playListRepository.save(playlists).length);
    // given playlistId 20 times
    var playlistIds = playlists.parallelStream().map(PlayList::getId).collect(Collectors.toList());

    // then save music to playlist
    playlistIds.forEach(
        id -> {
          assertEquals(num, playListMusicRepository.saveMusicsToPlayList(id, musics).length);
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
            assertEquals(
                playlistNum, creatorPlayListRepository.listPlaylistsByCreatorId(creatorId).size()),
        () ->
            // then delete playlist from creator
            assertEquals(
                playlistNum,
                creatorPlayListRepository.deletePlaylistsFromCreator(creatorId, playlistIds)
                    .length),
        // then delete playlist
        () -> assertEquals(playlistNum, playListRepository.deleteById(playlistIds)),
        // then delete music
        () -> assertEquals(num, musicRepository.delete(musicIds)));
  }
}
