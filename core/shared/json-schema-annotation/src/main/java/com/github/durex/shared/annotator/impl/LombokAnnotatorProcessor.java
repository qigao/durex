package com.github.durex.shared.annotator.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.durex.shared.annotator.AnnotatorProcessor;
import com.sun.codemodel.JDefinedClass;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LombokAnnotatorProcessor implements AnnotatorProcessor {

  public void classPropertyProcessor(JDefinedClass clazz, JsonNode schema) {
    var lombok = schema.get("lombok");
    if (lombok != null) {
      lombok
          .fieldNames()
          .forEachRemaining(
              property -> {
                var annotation = processLombokAnnotation(property);
                if (!Objects.equals(annotation, IllegalArgumentException.class)) {
                  clazz.annotate(annotation);
                }
              });
    }
  }

  /**
   * get the Lombok annotation for the given property name
   *
   * @param property the property name
   * @return the Lombok annotation class
   */
  @SuppressWarnings({"rawtypes"})
  private Class processLombokAnnotation(String property) {
    switch (property) {
      case "NoArgsConstructor":
        return NoArgsConstructor.class;
      case "AllArgsConstructor":
        return AllArgsConstructor.class;
      case "Data":
        return Data.class;
      case "Builder":
        return Builder.class;
      case "SuperBuilder":
        return SuperBuilder.class;
      case "Getter":
        return Getter.class;
      case "Setter":
        return Setter.class;
      case "With":
        return With.class;
      case "EqualsAndHashCode":
        return EqualsAndHashCode.class;
      case "ToString":
        return ToString.class;
      default:
        return IllegalArgumentException.class;
    }
  }
}
