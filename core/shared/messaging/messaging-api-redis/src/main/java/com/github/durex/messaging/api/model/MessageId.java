package com.github.durex.messaging.api.model;

import java.io.Serializable;

public interface MessageId<T> extends Comparable<T>, Serializable {
  T getMessageId();
}
