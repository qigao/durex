package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.support.DemoMusicData;
import com.github.durex.uniqid.uuid.UniqID;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.test.StepVerifier;

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
    musicRepository.save(musics).as(StepVerifier::create).expectNextCount(20).verifyComplete();
    var playList = DemoMusicData.givenAPlayList();
    playListRepository.save(playList).as(StepVerifier::create).expectNextCount(1).verifyComplete();

    var playlistId = playList.getId();

    assertAll(
        "save musics to a playlist",
        () ->
            playListMusicRepository
                .saveMusicsToPlayList(playlistId, musics)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete(),
        () ->
            playListMusicRepository
                .listMusicsByPlayListId(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete());
    var creatorId = UniqID.getId();
    assertAll(
        "save playlist to creator",
        () ->
            creatorPlayListRepository
                .savePlaylistToCreator(creatorId, playlistId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            creatorPlayListRepository
                .listPlaylistsByCreatorId(creatorId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            creatorPlayListRepository
                .deletePlaylistFromCreator(creatorId, playlistId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            playListMusicRepository
                .clearMusicsFromPlayList(playlistId)
                .as(StepVerifier::create)
                .expectNext(20)
                .verifyComplete(),
        () ->
            creatorPlayListRepository
                .listPlaylistsByCreatorId(creatorId)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete(),
        () ->
            playListRepository
                .deleteById(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());
  }

  @Test
  @Order(200)
  @DisplayName("When save multiple playlists to creator")
  void testSavePlayListToCreatorWithSamePlaylist() {
    // given 20 musics
    var musics = DemoMusicData.givenSomeMusics(20);
    List<String> musicIds = musics.parallelStream().map(Music::getId).collect(Collectors.toList());

    musicRepository.save(musics).as(StepVerifier::create).expectNextCount(20).verifyComplete();
    // given playlist with 20 musics
    var playlists = DemoMusicData.givenSomePlayList(20);
    playListRepository
        .save(playlists)
        .as(StepVerifier::create)
        .expectNextCount(20)
        .verifyComplete();
    // given playlistId 20 times
    var playlistIds = playlists.parallelStream().map(PlayList::getId).collect(Collectors.toList());

    // then save music to playlist
    playlistIds.forEach(
        id ->
            playListMusicRepository
                .saveMusicsToPlayList(id, musics)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete());

    // then save playlist to creator
    var creatorId = UniqID.getId();
    assertAll(
        "save playlist to creator",
        () ->
            creatorPlayListRepository
                .savePlaylistToCreator(creatorId, playlistIds)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete(),
        () ->
            creatorPlayListRepository
                .listPlaylistsByCreatorId(creatorId)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete(),
        () ->
            // then delete playlist from creator

            creatorPlayListRepository
                .deletePlaylistsFromCreator(creatorId, playlistIds)
                .as(StepVerifier::create)
                .expectNext(20)
                .verifyComplete(),
        // then delete playlist
        () ->
            playListRepository
                .deleteById(playlistIds)
                .as(StepVerifier::create)
                .expectNext(20)
                .verifyComplete(),
        // then delete music
        () ->
            musicRepository
                .delete(musicIds)
                .as(StepVerifier::create)
                .expectNext(20)
                .verifyComplete());
  }
}
