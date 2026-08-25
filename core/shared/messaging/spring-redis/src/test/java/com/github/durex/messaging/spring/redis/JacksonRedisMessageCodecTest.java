package com.github.durex.messaging.spring.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JacksonRedisMessageCodecTest {

  @Test
  void decodesParameterizedPayloadUsingDeclaredGenericType() throws Exception {
    var codec = new JacksonRedisMessageCodec(new JsonMapper());
    var expected = List.of(new Event("event-1", "hello"), new Event("event-2", "world"));
    Type targetType =
        GenericTarget.class.getDeclaredMethod("accept", List.class).getGenericParameterTypes()[0];

    var payload = codec.encode(expected);
    var decoded = codec.decode(payload, targetType);

    assertEquals(expected, decoded);
  }

  private static final class GenericTarget {
    @SuppressWarnings("unused")
    void accept(List<Event> events) {}
  }

  record Event(String id, String value) {}
}
