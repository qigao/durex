package com.github.durex.redisson;

import com.github.durex.redisson.service.api.RemoteServiceApi;
import com.github.durex.redisson.service.impl.RemoteServiceImpl;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

@Slf4j
@ApplicationScoped
public class ServiceLifecycleConfig {
  @Inject RedissonClient redissonClient;

  void onStart(@Observes StartupEvent event) {
    log.info("starting redisson remote service...");
    redissonClient.getRemoteService().register(RemoteServiceApi.class, new RemoteServiceImpl(), 2);
  }

  void onStop(@Observes ShutdownEvent ev) {
    log.info("The application is stopping...");
    redissonClient.shutdown();
  }
}
