package com.github.durex.messaging.test.service;

import com.github.durex.messaging.test.models.TestEvent;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStreamReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.client.RedisException;
import org.redisson.codec.JsonJacksonCodec;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class StreamService {
  public static final String TEST_GROUP = "test_group";
  public static final String TEST_CHANNEL = "test_4921";
  @Inject RedissonClient redisson;

  private RStreamReactive<String, TestEvent> stream;

  public void init() {
    this.stream = redisson.reactive().getStream(TEST_CHANNEL, new JsonJacksonCodec());
    try {
      stream.createGroup(TEST_GROUP).subscribe();
    } catch (RedisException e) {
      log.info("Redis group {} already exists.", TEST_GROUP);
    }
  }

  public Mono<StreamMessageId> sendReactiveMessage(TestEvent testEvent) {
    final var streamAddArgs = StreamAddArgs.entry("1", testEvent);
    return stream.add(streamAddArgs);
  }

  public void consumerReactiveMessage() {
    var messages = stream.readGroup(TEST_GROUP, "consumer_1", StreamReadGroupArgs.neverDelivered());
    messages
        .doOnNext(
            m -> {
              for (var entry : m.entrySet()) {
                Map<String, TestEvent> msg = entry.getValue();
                log.info("read entry: {}", msg);
              }
            })
        .subscribe();
  }
}
