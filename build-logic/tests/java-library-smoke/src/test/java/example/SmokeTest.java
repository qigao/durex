package example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTest {
  @Test
  void runsOnJunitPlatform() {
    assertEquals(4, 2 + 2);
  }
}
