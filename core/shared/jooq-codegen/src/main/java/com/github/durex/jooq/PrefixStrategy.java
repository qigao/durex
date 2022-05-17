package com.github.durex.jooq;

import lombok.extern.slf4j.Slf4j;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

@Slf4j
public class PrefixStrategy extends DefaultGeneratorStrategy {

  @Override
  public String getJavaClassName(Definition definition, Mode mode) {
    var outputName = super.getJavaClassName(definition, mode);
    switch (mode) {
      case POJO:
        return "P" + outputName;
      case RECORD:
        return "R" + outputName.replace("Record", "");
      case DEFAULT:
        if (definition instanceof TableDefinition) {
          return "Q" + outputName;
        }
        return outputName;
      default:
        return outputName;
    }
  }
}
