package com.github.durex.music.spring;

import com.github.durex.music.repository.CreatorPlayListRepository;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class MusicSpringConfiguration {

  @Bean
  MusicRepository musicRepository(DSLContext dsl) {
    return new MusicRepository(dsl);
  }

  @Bean
  PlayListRepository playListRepository(DSLContext dsl) {
    return new PlayListRepository(dsl);
  }

  @Bean
  PlayListMusicRepository playListMusicRepository(DSLContext dsl) {
    return new PlayListMusicRepository(dsl);
  }

  @Bean
  CreatorPlayListRepository creatorPlayListRepository(DSLContext dsl) {
    return new CreatorPlayListRepository(dsl);
  }

  @Bean
  MusicService musicService(MusicRepository repository) {
    return new MusicService(repository);
  }

  @Bean
  PlaylistService playlistService(
      PlayListRepository repository, PlayListMusicRepository playListMusicRepository) {
    return new SpringPlaylistService(repository, playListMusicRepository);
  }
}
