package com.github.durex.messaging.interceptor;

import com.github.durex.messaging.api.annotation.OutGoing;
import com.github.durex.messaging.api.annotation.Topic;
import com.github.durex.messaging.api.model.CodecEnum;
import com.github.durex.messaging.event.RedisStreamTopicPublisher;
import com.github.durex.messaging.redis.RedisCodec;
import com.github.durex.uniqid.UniqID;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
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
    var group = annotation.group();
    var returnType = method.getReturnType();
    log.info("return Type: {}", returnType.getSimpleName());
    var paramType = method.getParameterTypes();
    var simpleEvent = paramType[0];
    log.info("param types: {}", simpleEvent.getSimpleName());
    if (ObjectUtils.isEmpty(group)) {
      return result.doOnSuccess(
          v -> redissonClient.getTopic(topic, RedisCodec.getCodec(codec)).publishAsync(v));
    } else {
      return result.doOnSuccess(v -> publish(topic, codec, UniqID.getId(), v).subscribe());
    }
  }

  private Mono<?> getMethodResponse(InvocationContext context) throws Exception {
    return (Mono<?>) context.proceed();
  }

  private <T> Mono<StreamMessageId> publish(String topic, CodecEnum codec, String key, T event) {
    var publisher = new RedisStreamTopicPublisher(redissonClient, topic, codec);
    return publisher.publish(key, event);
  }
}
