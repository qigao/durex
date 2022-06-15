package com.github.durex.messaging.interceptor;

import com.github.durex.messaging.api.annotation.OutGoing;
import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.redis.RedisCodec;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

@Slf4j
@Interceptor
@OutGoing
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 1)
public class OutGoingInterceptor {
  public static final Class<OutGoing> ANNOTATION_CLASS = OutGoing.class;
  @Inject RedissonClient redissonClient;

  @AroundInvoke
  Object outGoingInvoke(InvocationContext context) throws Exception {
    final var method = context.getMethod();
    var result = getMethodResponse(context);
    var annotation = method.getAnnotation(ANNOTATION_CLASS).topic().getAnnotation(Topic.class);
    var topic = annotation.value();
    var codec = annotation.codec();
    return result.doOnSuccess(
        v -> redissonClient.getTopic(topic, RedisCodec.getCodec(codec)).publishAsync(v));
  }

  private Mono<?> getMethodResponse(InvocationContext context) throws Exception {
    return (Mono<?>) context.proceed();
  }
}
