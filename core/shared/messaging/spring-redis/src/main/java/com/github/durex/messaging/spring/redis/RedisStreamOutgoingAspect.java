package com.github.durex.messaging.spring.redis;

import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

@Aspect
public class RedisStreamOutgoingAspect {
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final Environment environment;

  public RedisStreamOutgoingAspect(
      StringRedisTemplate redisTemplate, JsonMapper jsonMapper, Environment environment) {
    this.redisTemplate = redisTemplate;
    this.jsonMapper = jsonMapper;
    this.environment = environment;
  }

  @Around("@annotation(outgoing)")
  public Object publishResult(ProceedingJoinPoint joinPoint, RedisStreamOutgoing outgoing)
      throws Throwable {
    Object result = joinPoint.proceed();
    if (result != null) {
      String stream = environment.resolvePlaceholders(outgoing.value());
      String payload = jsonMapper.writeValueAsString(result);
      redisTemplate.opsForStream().add(MapRecord.create(stream, Map.of("payload", payload)));
    }
    return result;
  }
}
