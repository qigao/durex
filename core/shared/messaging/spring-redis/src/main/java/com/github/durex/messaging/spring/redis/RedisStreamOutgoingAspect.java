package com.github.durex.messaging.spring.redis;

import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

@Aspect
public class RedisStreamOutgoingAspect {
  private final StringRedisTemplate redisTemplate;
  private final RedisMessageCodec messageCodec;
  private final Environment environment;

  public RedisStreamOutgoingAspect(
      StringRedisTemplate redisTemplate, RedisMessageCodec messageCodec, Environment environment) {
    this.redisTemplate = redisTemplate;
    this.messageCodec = messageCodec;
    this.environment = environment;
  }

  @Around("@annotation(outgoing)")
  public Object publishResult(ProceedingJoinPoint joinPoint, RedisStreamOutgoing outgoing)
      throws Throwable {
    Object result = joinPoint.proceed();
    if (result != null) {
      String stream = environment.resolvePlaceholders(outgoing.value());
      String payload = messageCodec.encode(result);
      TransactionalPublication.publishNowOrAfterCommit(
          () -> redisTemplate.opsForStream().add(MapRecord.create(stream, Map.of("payload", payload))));
    }
    return result;
  }
}
