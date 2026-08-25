package com.github.durex.reference.springnative;

import java.time.Duration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messaging")
public class NativeMessagingController {
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final NativeMessagingPipeline pipeline;

  public NativeMessagingController(NativeMessagingPipeline pipeline) {
    this.pipeline = pipeline;
  }

  @PostMapping("/pubsub/{value}")
  public String pubSub(@PathVariable String value) throws InterruptedException {
    pipeline.publishPubSub(value);
    return receivedValue(pipeline.awaitPubSub(TIMEOUT), "Pub/Sub");
  }

  @PostMapping("/stream/{value}")
  public String stream(@PathVariable String value) throws InterruptedException {
    pipeline.publishStream(value);
    return receivedValue(pipeline.awaitStream(TIMEOUT), "Redis Stream");
  }

  private static String receivedValue(NativeMessage received, String transport) {
    if (received == null) {
      throw new IllegalStateException(transport + " message was not received before timeout");
    }
    return received.value();
  }
}
