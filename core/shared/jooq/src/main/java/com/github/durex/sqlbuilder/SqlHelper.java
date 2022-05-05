package com.github.durex.sqlbuilder;

import com.github.durex.sqlbuilder.enums.WildCardType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SqlHelper {
  public String likeClauseBuilder(WildCardType wildCardEnum, String realTitle) {
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
