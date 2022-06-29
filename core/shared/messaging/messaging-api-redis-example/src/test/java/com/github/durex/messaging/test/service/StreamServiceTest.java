package com.github.durex.messaging.test.service;

import com.github.durex.messaging.test.models.TestEvent;
import io.quarkus.test.junit.QuarkusTest;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.Name;
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
  void testStreamReactiveMessage() throws InterruptedException {
    final Name fakeName = Faker.instance().name();
    for (int i = 0; i < 5; i++) {
      int age = i * 2 + 1;
      var event =
          TestEvent.builder().age(age).name(fakeName.name()).build();
      TimeUnit.MILLISECONDS.sleep(50);

      var id = streamService.pubMessage(event);
      id.subscribe(o -> log.info("send Id: {}", o));
    }
    TimeUnit.MILLISECONDS.sleep(1000);
    for (int i = 0; i < 5; i++) {
      int age = i * 2 + 1;
      var event =
          TestEvent.builder().age(age).name(fakeName.name()).build();
      var id = streamService.pubMessage(event);
      id.subscribe(o -> log.info("send Id: {}", o));
    }
  }
}
