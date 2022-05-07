package com.github.durex.jooq;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;

public class PrefixStrategy extends DefaultGeneratorStrategy {

  @Override
  public String getJavaClassName(Definition definition, Mode mode) {
    var outputName = super.getJavaClassName(definition, mode);
    switch (mode) {
      case POJO:
        return "Q" + outputName;
      case RECORD:
        return "R" + outputName.replace("Record", "");
      default:
        return "T" + outputName;
    }
  }
}
