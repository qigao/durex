package com.github.durex.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PairTest {
  @Test
  void testPair() {
    var pair = Pair.of("a", 1);
    assertEquals("a", pair.getFirst());
    assertEquals(1, pair.getSecond());
  }
}
