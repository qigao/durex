package com.github.durex.messaging.api.model;

import java.time.Instant;
import java.util.Map;
import org.redisson.api.StreamMessageId;

public interface RedisMessage<T> {
  T getPayload();

  Map<String, String> getProperties();

  boolean hasProperty(String var1);

  String getProperty(String var1);

  StreamMessageId getMessageId();

  Instant getPublishTime();

  String getProducerName();

  String getTopicName();

  long getSequenceId();
}
