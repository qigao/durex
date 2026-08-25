package com.github.durex.messaging.spring.redis;

import java.lang.reflect.Type;

public interface RedisMessageCodec {
  String encode(Object value) throws Exception;

  Object decode(String payload, Type targetType) throws Exception;
}
