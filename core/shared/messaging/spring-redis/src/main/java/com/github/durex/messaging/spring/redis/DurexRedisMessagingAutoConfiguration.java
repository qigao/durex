package com.github.durex.messaging.spring.redis;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.messaging.RedisMessageSendingOperations;
import org.springframework.data.redis.messaging.RedisMessageSendingTemplate;

@AutoConfiguration(afterName = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@ConditionalOnClass({RedisMessageSendingTemplate.class, Aspect.class})
@ConditionalOnBean(RedisConnectionFactory.class)
public class DurexRedisMessagingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(RedisMessageSendingOperations.class)
  RedisMessageSendingOperations durexRedisMessageSendingOperations(
      RedisConnectionFactory connectionFactory) {
    return new RedisMessageSendingTemplate(new StringRedisTemplate(connectionFactory));
  }

  @Bean
  @ConditionalOnMissingBean(RedisOutgoingAspect.class)
  RedisOutgoingAspect redisOutgoingAspect(
      RedisMessageSendingOperations messageSendingOperations, Environment environment) {
    return new RedisOutgoingAspect(messageSendingOperations, environment);
  }
}
