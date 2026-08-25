package com.github.durex.messaging.spring.redis;

import java.lang.reflect.Type;
import tools.jackson.databind.json.JsonMapper;

public final class JacksonRedisMessageCodec implements RedisMessageCodec {
  private final JsonMapper jsonMapper;

  public JacksonRedisMessageCodec(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public String encode(Object value) throws Exception {
    return jsonMapper.writeValueAsString(value);
  }

  @Override
  public Object decode(String payload, Type targetType) throws Exception {
    return jsonMapper.readValue(payload, jsonMapper.constructType(targetType));
  }
}
