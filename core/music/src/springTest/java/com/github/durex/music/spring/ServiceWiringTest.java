package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class ServiceWiringTest {
  @Autowired MusicService musicService;
  @Autowired PlaylistService playlistService;

  @Test
  void servicesUseSpringWiringAndPlaylistServiceIsTransactionalProxy() {
    assertNotNull(musicService);
    assertNotNull(playlistService);
    assertTrue(AopUtils.isAopProxy(playlistService));
  }
}
