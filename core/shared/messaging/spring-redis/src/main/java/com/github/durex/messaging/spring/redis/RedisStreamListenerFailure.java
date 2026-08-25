package com.github.durex.messaging.spring.redis;

import java.lang.reflect.Method;
import org.springframework.data.redis.connection.stream.MapRecord;

public record RedisStreamListenerFailure(
    String stream,
    String group,
    MapRecord<String, String, String> record,
    Method method,
    Throwable cause) {}
