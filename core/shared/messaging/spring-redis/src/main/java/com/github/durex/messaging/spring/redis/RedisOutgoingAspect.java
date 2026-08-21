package com.github.durex.messaging.spring.redis;

import com.github.durex.messaging.annotation.Outgoing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.messaging.RedisMessageSendingOperations;

@Aspect
public class RedisOutgoingAspect {
  private final RedisMessageSendingOperations messageSendingOperations;
  private final Environment environment;

  public RedisOutgoingAspect(
      RedisMessageSendingOperations messageSendingOperations, Environment environment) {
    this.messageSendingOperations = messageSendingOperations;
    this.environment = environment;
  }

  @Around("@annotation(outgoing)")
  public Object publishResult(ProceedingJoinPoint joinPoint, Outgoing outgoing) throws Throwable {
    Object result = joinPoint.proceed();
    if (result != null) {
      String destination = environment.resolvePlaceholders(outgoing.value());
      messageSendingOperations.convertAndSend(destination, result);
    }
    return result;
  }
}
