package com.github.durex.sqlbuilder.enums;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class WildCardTypeTest {
  @ParameterizedTest
  @EnumSource(WildCardType.class)
  void testWildCardEnumType(WildCardType wildCardType) {
    assertNotNull(wildCardType);
  }
}
