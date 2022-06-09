package com.github.durex.redisson;



 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.ws.rs.Produces;

 import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;

@ApplicationScoped
public final class ReactiveRedissonProducer {
  @Inject
  RedissonClient redissonClient;

  @Produces
  public RedissonReactiveClient getReactiveRedisson() {
    return redissonClient.reactive();
  }
}
