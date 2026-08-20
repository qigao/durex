package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.durex.music.model.Music;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.service.MusicService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MusicServiceConstructionTest {

  @Test
  void serviceCanBeConstructedWithoutFrameworkContainer() {
    var repository = mock(MusicRepository.class);
    var music = new Music();
    music.setId("music-1");
    when(repository.findById("music-1")).thenReturn(Optional.of(music));

    var service = new MusicService(repository);

    assertSame(music, service.getMusicById("music-1"));
  }
}
