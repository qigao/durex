package com.github.durex.messaging.redis;

import com.github.durex.messaging.api.model.MessageId;

public class RedisMessageId implements MessageId<Long> {
  private Long messageId;

  @Override
  public Long getMessageId() {
    return messageId;
  }

  public void setMessageId(Long id) {
    this.messageId = id;
  }

  @Override
  public int compareTo(Long aLong) {
    return aLong.compareTo(messageId);
  }
}
