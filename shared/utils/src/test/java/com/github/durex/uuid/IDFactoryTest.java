package com.github.durex.uuid;

import static com.github.durex.uuid.UniqID.getId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.utils.IDType;
import com.github.f4b6a3.ulid.Ulid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class IDFactoryTest {

  @ParameterizedTest
  @EnumSource(IDType.class)
  void testUniqID(IDType type) {
    assertNotNull(getId(type));
  }

  @Test
  void testUlid() {
    String id = getId(IDType.ULID);
    long time = Ulid.from(id).getTime();
    LocalDateTime triggerTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
    LocalDateTime actualDateTime = LocalDateTime.now();
    assertThat(actualDateTime, LocalDateTimeMatchers.within(1, ChronoUnit.SECONDS, triggerTime));
  }
}
