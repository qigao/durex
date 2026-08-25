package com.github.durex.reference.springnative;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringNativeReferenceApplication.class)
class NativeMessagingIntegrationTest {

  @Autowired NativeMessagingPipeline pipeline;

  @Test
  void pubSubRoundTripsThroughDurexOutgoingAndSpringRedisListener() throws Exception {
    pipeline.publishPubSub("native-pubsub");
    assertEquals("native-pubsub", pipeline.awaitPubSub(Duration.ofSeconds(5)).value());
  }

  @Test
  void streamRoundTripsThroughDurexOutgoingAndReflectiveStreamListener() throws Exception {
    pipeline.publishStream("native-stream");
    assertEquals("native-stream", pipeline.awaitStream(Duration.ofSeconds(5)).value());
  }
}
