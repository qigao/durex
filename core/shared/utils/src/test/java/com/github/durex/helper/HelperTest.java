package com.github.durex.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HelperTest {

  @Test
  void testToOptional() {
    assertEquals(Optional.of(1), Helper.toOptional(1));
    assertEquals(Optional.empty(), Helper.toOptional(null));
  }

  @Test
  void testReadResource() throws IOException {
    var content = Helper.readResource("/hello.txt", getClass()).orElse("");
    assertEquals("Hello", content.trim());
  }

  @Test
  void testReadResourceWithoutFile() throws IOException {
    var content = Helper.readResource("/hello1.txt", getClass());
    assertTrue(content.isEmpty());
  }
}
