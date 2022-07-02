package com.github.durex.shared.annotator;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

public interface AnnotatorProcessor {
  static void classPropertyProcessor(JDefinedClass clazz, JsonNode schema) {}

  static void processSwaggerSchemaField(JFieldVar field, JsonNode propertyNode) {}
}
