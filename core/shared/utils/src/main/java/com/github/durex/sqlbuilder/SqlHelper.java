package com.github.durex.sqlbuilder;

import com.github.durex.sqlbuilder.enums.WildCardType;

public final class SqlHelper {
  private SqlHelper() {}

  public static String likeClauseBuilder(WildCardType wildCardEnum, String realTitle) {
    switch (wildCardEnum) {
      case START_WITH:
        realTitle = realTitle + "%";
        break;
      case END_WITH:
        realTitle = "%" + realTitle;
        break;
      case CONTAINS:
        realTitle = "%" + realTitle + "%";
        break;
      default:
        break;
    }
    return realTitle;
  }
}
