package com.github.durex.shared.interceptors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.github.durex.shared.annotations.Cached;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonClient;

@Interceptor
@Priority(1000)
@Cached
@Slf4j
public class CachedInterceptor {

  @Inject RedissonClient redissonClient;

  @AroundInvoke
  public Object aroundInvoke(InvocationContext ctx) throws Exception {
    var method = ctx.getMethod();
    var key = method.getAnnotation(Cached.class).key();
    var clazz = method.getAnnotation(Cached.class).clazz();
    var redisson = redissonClient.reactive();
//    RMapReactive<String, clazz> map = redisson.getMap(clazz.getName());
//    var value = map.get(key);
//    if (value != null) {
//      log.info("cache hit: {}", key);
//      return value;
//    }
//    log.info("cache miss: {}", key);
//    var result = ctx.proceed();
//    redisson.getBucket(key).set(result);
    return ctx.proceed();
  }
}
