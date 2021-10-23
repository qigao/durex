package com.github.durex.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SerDe {

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  static {
    jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    jsonMapper.registerModule(new JavaTimeModule());
  }

  public static <T> T toObject(final String json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final byte[] json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final URL json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final File json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final InputStream json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Reader json, final Class<T> type) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final String json, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final byte[] json, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final URL json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final File json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final InputStream json, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final Reader json, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static String toString(Object object) throws IOException {
    return jsonMapper.writeValueAsString(object);
  }

  public static byte[] toBytes(Object object) throws IOException {
    return jsonMapper.writeValueAsBytes(object);
  }

  public static void toFile(Object object, File json) throws IOException {
    jsonMapper.writeValue(json, object);
  }

  public static void toFileWithPretty(Object object, File json) throws IOException {
    jsonMapper.writerWithDefaultPrettyPrinter().writeValue(json, object);
  }

  public static void toWriter(Object object, Writer writer) throws IOException {
    jsonMapper.writeValue(writer, object);
  }

  public static void toOutputStream(Object object, final OutputStream json) throws IOException {
    jsonMapper.writeValue(json, object);
  }

  public static <T> T findJsonNode(final String json, final String jsonPath, final Class<T> type)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(final byte[] json, final String jsonPath, final Class<T> type)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(final URL json, final String jsonPath, final Class<T> type)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(final File json, final String jsonPath, final Class<T> type)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(
      final InputStream json, final String jsonPath, final Class<T> type) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(final Reader json, final String jsonPath, final Class<T> type)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    return jsonMapper.treeToValue(node.at(jsonPath), type);
  }

  public static <T> T findJsonNode(
      final String json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }

  public static <T> T findJsonNode(
      final byte[] json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }

  public static <T> T findJsonNode(
      final InputStream json, final String jsonPath, final TypeReference<T> typeRef)
      throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }

  public static <T> T findJsonNode(
      final URL json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }

  public static <T> T findJsonNode(
      final File json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }

  public static <T> T findJsonNode(
      final Reader json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    JsonNode node = jsonMapper.readTree(json);
    JavaType jt = jsonMapper.getTypeFactory().constructType(typeRef);
    return jsonMapper.readValue(jsonMapper.treeAsTokens(node.at(jsonPath)), jt);
  }
}
