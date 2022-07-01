package com.github.durex.uniqid;

import static org.hamcrest.MatcherAssert.assertThat;

import com.github.f4b6a3.ulid.Ulid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.Test;

class IDFactoryTest {

  @Test
  void testUlid() {
    String id = UniqID.getId();
    long time = Ulid.from(id).getTime();
    LocalDateTime triggerTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
    LocalDateTime current = LocalDateTime.now();
    assertThat(triggerTime, LocalDateTimeMatchers.before(current));
  }
}
