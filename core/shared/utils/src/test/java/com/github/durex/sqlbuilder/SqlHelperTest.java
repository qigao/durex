package com.github.durex.sqlbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.sqlbuilder.enums.WildCardType;
import org.junit.jupiter.api.Test;

class SqlHelperTest {

  @Test
  void testLikeClauseBuilder() {
    assertEquals("%b%", SqlHelper.likeClauseBuilder(WildCardType.CONTAINS, "b"));
    assertEquals("%b", SqlHelper.likeClauseBuilder(WildCardType.END_WITH, "b"));
    assertEquals("b", SqlHelper.likeClauseBuilder(WildCardType.NONE, "b"));
    assertEquals("b%", SqlHelper.likeClauseBuilder(WildCardType.START_WITH, "b"));
  }
}
