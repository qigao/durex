package com.github.durex.reference.springnative;

import com.github.durex.messaging.annotation.Outgoing;
import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class NativeMessagingPipeline implements SmartInitializingSingleton {
  static final String PUBSUB_TOPIC = "native.messages.pubsub";
  static final String STREAM = "native.messages.stream";
  static final String STREAM_GROUP = "native-workers";

  private final StringRedisTemplate redisTemplate;
  private final BlockingQueue<NativeMessage> pubSubReceived = new LinkedBlockingQueue<>();
  private final BlockingQueue<NativeMessage> streamReceived = new LinkedBlockingQueue<>();

  public NativeMessagingPipeline(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void afterSingletonsInstantiated() {
    try {
      redisTemplate.opsForStream().createGroup(STREAM, ReadOffset.from("0-0"), STREAM_GROUP);
    } catch (RedisSystemException exception) {
      if (!causedByBusyGroup(exception)) {
        throw exception;
      }
    }
  }

  @RedisListener(topic = PUBSUB_TOPIC, consumes = "application/json")
  public void receivePubSub(NativeMessage message) {
    pubSubReceived.add(message);
  }

  @Outgoing(PUBSUB_TOPIC)
  public NativeMessage publishPubSub(String value) {
    return new NativeMessage(value);
  }

  @RedisStreamListener(
      stream = STREAM,
      group = STREAM_GROUP,
      consumer = "native-worker-1",
      autoAck = false)
  public void receiveStream(NativeMessage message) {
    streamReceived.add(message);
  }

  @RedisStreamOutgoing(STREAM)
  public NativeMessage publishStream(String value) {
    return new NativeMessage(value);
  }

  public NativeMessage awaitPubSub(Duration timeout) throws InterruptedException {
    return pubSubReceived.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  public NativeMessage awaitStream(Duration timeout) throws InterruptedException {
    return streamReceived.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  private static boolean causedByBusyGroup(Throwable failure) {
    for (Throwable current = failure; current != null; current = current.getCause()) {
      String message = current.getMessage();
      if (message != null && message.contains("BUSYGROUP")) {
        return true;
      }
    }
    return false;
  }
}
