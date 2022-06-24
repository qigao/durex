package com.github.durex.messaging.test.service;

import com.github.durex.messaging.test.models.TestEvent;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
class StreamServiceTest {
  @Inject StreamService streamService;

  @BeforeEach
  void setUp() {
    streamService.init();
  }

  @Test
  void testStreamReactiveMessage() {
    for (int i = 0; i < 5; i++) {
      int age = i * 2 + 1;
      var event =
          TestEvent.builder().age(String.valueOf(age)).name(Faker.instance().name().name()).build();
      var id = streamService.sendReactiveMessage(event);
      id.subscribe(o -> log.info("send Id: {}", o));
    }
    streamService.consumerReactiveMessage();
  }
}
