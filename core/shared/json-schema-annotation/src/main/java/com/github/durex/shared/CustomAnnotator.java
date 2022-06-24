package com.github.durex.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * CustomAnnotator for json schema files, so that we can add our own annotations to the generated
 * classes.
 */
@SuppressWarnings({"squid:S3740", "squid:S1191"})
public class CustomAnnotator extends AbstractAnnotator {

  /**
   * try to set up annotations for the given class from additionalProperties, such as Lombok
   * annotations, Swagger annotations, etc.
   *
   * @param clazz a generated pojo class, that is serialized to JSON
   * @param schema the object schema associated with this clazz
   */
  @Override
  public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
    lombokProperty(clazz, schema);
    swaggerProperty(clazz, schema);
  }

  private void swaggerProperty(JDefinedClass clazz, JsonNode schema) {
    JsonNode swaggerSchema = schema.get("swagger-schema");
    if (swaggerSchema != null) {
      var annotation = clazz.annotate(Schema.class);
      swaggerSchema
          .fieldNames()
          .forEachRemaining(
              property -> annotation.param(property, swaggerSchema.get(property).asText()));
    }
  }

  private void lombokProperty(JDefinedClass clazz, JsonNode schema) {
    JsonNode lombok = schema.get("lombok");
    if (lombok != null) {
      lombok
          .fieldNames()
          .forEachRemaining(
              property -> {
                var annotation = processClassAnnotation(property);
                if (!Objects.equals(annotation, IllegalArgumentException.class)) {
                  clazz.annotate(annotation);
                }
              });
    }
  }

  /**
   * disable AdditionalProperties as we'll use this for class annotations.
   *
   * @return false
   */
  @Override
  public boolean isAdditionalPropertiesSupported() {
    return false;
  }

  /**
   * get the Lombok annotation for the given property name
   *
   * @param property the property name
   * @return the Lombok annotation class
   */
  private Class processClassAnnotation(String property) {
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

  /**
   * get the property annotation for the given property name
   *
   * @param field the field that contains data that will be serialized
   * @param clazz the owner of the field (class to which the field belongs)
   * @param propertyName the name of the JSON property that this field represents
   * @param propertyNode the schema node defining this property
   */
  @Override
  public void propertyField(
      JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
    super.propertyField(field, clazz, propertyName, propertyNode);
    if (propertyNode.has("schema")) {
      processSwaggerSchemaField(field, propertyNode);
    }
  }

  /**
   * process the Swagger schema field for the given field
   *
   * @param field the field that contains data that will be serialized
   * @param propertyNode the schema node defining this property
   */
  private void processSwaggerSchemaField(JFieldVar field, JsonNode propertyNode) {
    final JAnnotationUse annotate = field.annotate(Schema.class);
    propertyNode
        .get("schema")
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
