package com.github.durex.shared.annotator.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.durex.shared.annotator.AnnotatorProcessor;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SwaggerAnnotatorProcessor implements AnnotatorProcessor {

  public static final String SWAGGER = "swagger";

  public void classPropertyProcessor(JDefinedClass clazz, JsonNode schema) {
    var swagger = schema.get(SWAGGER);
    if (swagger != null) {
      var annotation = clazz.annotate(Schema.class);
      swagger
          .fieldNames()
          .forEachRemaining(property -> annotation.param(property, swagger.get(property).asText()));
    }
  }
  /**
   * process the Swagger schema field for the given field
   *
   * @param field the field that contains data that will be serialized
   * @param propertyNode the schema node defining this property
   */
  public void processSwaggerSchemaField(JFieldVar field, JsonNode propertyNode) {
    if (propertyNode.has(SWAGGER)) {
      final JAnnotationUse annotate = field.annotate(Schema.class);
      propertyNode
          .get(SWAGGER)
          .fields()
          .forEachRemaining(
              schemaField -> {
                switch (schemaField.getKey()) {
                  case "required":
                    annotate.param("required", schemaField.getValue().asBoolean());
                    break;
                  case "maxLength":
                    annotate.param("maxLength", schemaField.getValue().asInt());
                    break;
                  case "minLength":
                    annotate.param("minLength", schemaField.getValue().asInt());
                    break;
                  case "nullable":
                    annotate.param("nullable", schemaField.getValue().asBoolean());
                    break;
                  default:
                    annotate.param(schemaField.getKey(), schemaField.getValue().asText());
                    break;
                }
              });
    }
  }
}
