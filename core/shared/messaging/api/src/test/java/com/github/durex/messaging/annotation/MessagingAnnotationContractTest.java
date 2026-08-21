package com.github.durex.messaging.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MessagingAnnotationContractTest {

  @Test
  void annotationsAreRuntimeMethodMetadataWithoutFrameworkCoupling() {
    assertNeutralRuntimeMethodAnnotation(Outgoing.class);
    assertNeutralRuntimeMethodAnnotation(RedisStreamListener.class);
    assertNeutralRuntimeMethodAnnotation(RedisStreamOutgoing.class);
  }

  @Test
  void streamListenerRequiresExplicitDeliveryCoordinates() throws Exception {
    assertEquals(String.class, RedisStreamListener.class.getMethod("stream").getReturnType());
    assertEquals(String.class, RedisStreamListener.class.getMethod("group").getReturnType());
    assertEquals(String.class, RedisStreamListener.class.getMethod("consumer").getReturnType());
    assertEquals(false, RedisStreamListener.class.getMethod("autoAck").getDefaultValue());
  }

  private static void assertNeutralRuntimeMethodAnnotation(Class<? extends Annotation> type) {
    assertEquals(RUNTIME, type.getAnnotation(Retention.class).value());
    assertTrue(Arrays.asList(type.getAnnotation(Target.class).value()).contains(METHOD));
    assertFalse(
        Arrays.stream(type.getAnnotations())
            .map(annotation -> annotation.annotationType().getName())
            .anyMatch(
                name ->
                    name.startsWith("javax.")
                        || name.startsWith("jakarta.enterprise")
                        || name.startsWith("org.springframework")));
  }
}
