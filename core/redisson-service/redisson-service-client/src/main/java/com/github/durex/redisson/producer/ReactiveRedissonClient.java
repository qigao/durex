package com.github.durex.redisson.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;

@Singleton
public final class ReactiveRedissonClient {
  @Inject RedissonClient redissonClient;

  @Produces
  @ApplicationScoped
  public RedissonReactiveClient getReactiveRedisson() {
    return redissonClient.reactive();
  }
}
