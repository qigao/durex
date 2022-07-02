package com.github.durex.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.durex.shared.annotator.impl.LombokAnnotatorProcessor;
import com.github.durex.shared.annotator.impl.SwaggerAnnotatorProcessor;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
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
    LombokAnnotatorProcessor.classPropertyProcessor(clazz, schema);
    SwaggerAnnotatorProcessor.classPropertyProcessor(clazz, schema);
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
    SwaggerAnnotatorProcessor.processSwaggerSchemaField(field, propertyNode);
  }
}
