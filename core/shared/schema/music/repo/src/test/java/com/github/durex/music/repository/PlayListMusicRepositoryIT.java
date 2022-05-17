package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.api.Music;
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
    var result = musicRepository.save(musics);
    assertEquals(20, result.length);
    var playList = DemoMusicData.givenAPlayList();
    var savedPlaylist = playListRepository.save(playList);
    assertEquals(1, savedPlaylist);
    var playlistId = playList.getId();
    assertAll(
        "save musics to playlist",
        () -> assertEquals(20, repository.saveMusicsToPlayList(playlistId, musics).length),
        () -> assertEquals(20, repository.listMusicsByPlayListId(playlistId).size()));
    var musicIds = musics.stream().map(Music::getId).collect(Collectors.toList());
    var id = musicIds.get(0);
    var someIds = musicIds.stream().skip(1).limit(3).collect(Collectors.toList());
    assertAll(
        "delete musics from playlist",
        () -> assertEquals(1, repository.deleteMusicFromPlayList(playlistId, id)),
        () -> assertEquals(3, repository.deleteMusicFromPlayList(playlistId, someIds)),
        () -> assertEquals(16, repository.listMusicsByPlayListId(playlistId).size()),
        () -> assertEquals(16, repository.clearMusicsFromPlayList(playlistId)),
        () -> assertEquals(0, repository.listMusicsByPlayListId(playlistId).size()));
  }
}
