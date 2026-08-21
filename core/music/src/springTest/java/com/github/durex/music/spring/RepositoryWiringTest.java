package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.music.repository.CreatorPlayListRepository;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
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
}
