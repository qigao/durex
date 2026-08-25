package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

@SpringBootTest(classes = MusicSpringApplication.class)
class ServiceWiringTest {
  @Autowired MusicService musicService;
  @Autowired PlaylistService playlistService;
  @Autowired ApplicationContext context;

  @Test
  void servicesUseSpringWiringAndPlaylistServiceIsTransactionalProxy() throws Exception {
    assertNotNull(musicService);
    assertNotNull(playlistService);
    assertFalse(context.getBeansOfType(PlatformTransactionManager.class).isEmpty());

    var advisor = context.getBean(BeanFactoryTransactionAttributeSourceAdvisor.class);
    var method = PlaylistService.class.getMethod("findPlayList");
    assertTrue(advisor.getPointcut().getMethodMatcher().matches(method, SpringPlaylistService.class));
    assertTrue(AopUtils.isAopProxy(playlistService));
  }
}
