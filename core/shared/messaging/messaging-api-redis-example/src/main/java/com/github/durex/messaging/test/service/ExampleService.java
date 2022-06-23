package com.github.durex.messaging.test.service;

import com.github.durex.messaging.api.annotation.InComing;
import com.github.durex.messaging.api.annotation.OutGoing;
import com.github.durex.messaging.test.models.DemoEvent;
import com.github.durex.messaging.test.models.TestEvent;
import com.github.durex.messaging.test.topics.DemoEventTopic;
import com.github.durex.messaging.test.topics.TestEventTopic;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@ApplicationScoped
public class ExampleService {
  @InComing(topic = DemoEventTopic.class)
  public void executeExample(DemoEvent demoEvent) {
    log.info("Executing Example {}", demoEvent);
  }

  @InComing(topic = TestEventTopic.class)
  public void executeTest(TestEvent testEvent) {
    log.info("Executing test event handler");
  }

  @OutGoing(topic = TestEventTopic.class)
  public Mono<TestEvent> sendTestEvent(TestEvent testEvent) {
    log.info("Sending test event: {}", testEvent);
    return Mono.just(testEvent);
  }
}
