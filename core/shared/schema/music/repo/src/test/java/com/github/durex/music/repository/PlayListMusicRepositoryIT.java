package com.github.durex.music.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.music.api.Music;
import com.github.durex.music.support.DemoMusicData;
import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
  void testSaveMusicsToPlayList() {
    var musics = DemoMusicData.givenSomeMusics(20);
    var result = musicRepository.save(musics);
    log.info("save result: {}", result);
    Assertions.assertEquals(20, result.length);
    var playList = DemoMusicData.givenAPlayList();
    var savedPlaylist = playListRepository.save(playList);
    Assertions.assertEquals(1, savedPlaylist);
    var playlistId = playList.getId();
    var saved = repository.saveMusicsToPlayList(playlistId, musics);
    assertEquals(20, saved.length);
    var playlists = repository.listMusicsByPlayListId(playlistId);
    assertEquals(20, playlists.size());
    var musicIds = musics.stream().map(Music::getId).collect(Collectors.toList());
    var id = musicIds.get(0);
    var someIds = musicIds.stream().skip(1).limit(3).collect(Collectors.toList());
    Assertions.assertEquals(1, repository.deleteMusicFromPlayList(playlistId, id));
    Assertions.assertEquals(3, repository.deleteMusicFromPlayList(playlistId, someIds));
    assertEquals(16, repository.listMusicsByPlayListId(playlistId).size());
    assertEquals(16, repository.clearMusicsFromPlayList(playlistId));
    assertEquals(0, repository.listMusicsByPlayListId(playlistId).size());
  }
}
