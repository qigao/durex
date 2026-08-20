package com.github.durex.music.spring;

import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.service.MusicService;
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
  MusicService musicService(MusicRepository repository) {
    return new MusicService(repository);
  }
}
