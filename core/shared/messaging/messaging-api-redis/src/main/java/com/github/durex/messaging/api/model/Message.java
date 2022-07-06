package com.github.durex.messaging.api.model;

import java.time.Instant;
import java.util.Map;

public interface Message<T, K> {
  T getPayload();

  Map<String, String> getProperties();

  boolean hasProperty(String var1);

  String getProperty(String var1);

  K getMessageId();

  Instant getPublishTime();

  String getProducerName();

  String getTopicName();

  long getSequenceId();
}
