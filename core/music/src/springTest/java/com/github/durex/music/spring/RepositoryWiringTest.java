package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.repository.CreatorPlayListRepository;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class RepositoryWiringTest {
  @Autowired MusicRepository musicRepository;
  @Autowired PlayListRepository playListRepository;
  @Autowired PlayListMusicRepository playListMusicRepository;
  @Autowired CreatorPlayListRepository creatorPlayListRepository;

  @Test
  void repositoriesAreConstructedBySpringWithoutCdi() {
    assertNotNull(musicRepository);
    assertNotNull(playListRepository);
    assertNotNull(playListMusicRepository);
    assertNotNull(creatorPlayListRepository);
  }

  @Test
  void findAllExcludesSoftDeletedRows() {
    assertEquals(
        List.of("music-1"), musicRepository.findAll().stream().map(Music::getId).toList());
    assertEquals(
        List.of("playlist-1"),
        playListRepository.findAll().stream().map(PlayList::getId).toList());
  }
}
