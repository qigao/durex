package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import com.github.durex.music.model.Music;
import com.github.durex.music.support.DemoMusicData;
import io.quarkus.test.junit.QuarkusTest;
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
class PlayListMusicRepositoryIT {
  @Inject PlayListMusicRepository repository;
  @Inject MusicRepository musicRepository;
  @Inject PlayListRepository playListRepository;

  @Test
  @Order(100)
  @DisplayName("When save musics to playlist")
  void testSaveMusicsToPlayList() {
    var musics = DemoMusicData.givenSomeMusics(20);
    musicRepository.save(musics).as(StepVerifier::create).expectNextCount(20).verifyComplete();
    var playList = DemoMusicData.givenAPlayList();
    playListRepository.save(playList).as(StepVerifier::create).expectNextCount(1).verifyComplete();

    var playlistId = playList.getId();
    assertAll(
        "save musics to playlist",
        () ->
            repository
                .saveMusicsToPlayList(playlistId, musics)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete(),
        () ->
            repository
                .listMusicsByPlayListId(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(20)
                .verifyComplete());
    var musicIds = musics.stream().map(Music::getId).collect(Collectors.toList());
    var id = musicIds.get(0);
    var someIds = musicIds.stream().skip(1).limit(3).collect(Collectors.toList());
    assertAll(
        "delete musics from playlist",
        () ->
            repository
                .deleteMusicFromPlayList(playlistId, id)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete(),
        () ->
            repository
                .deleteMusicFromPlayList(playlistId, someIds)
                .as(StepVerifier::create)
                .expectNext(3)
                .verifyComplete(),
        () ->
            repository
                .listMusicsByPlayListId(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(16)
                .verifyComplete(),
        () ->
            repository
                .clearMusicsFromPlayList(playlistId)
                .as(StepVerifier::create)
                .expectNext(16)
                .verifyComplete(),
        () ->
            repository
                .listMusicsByPlayListId(playlistId)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete());
  }
}
