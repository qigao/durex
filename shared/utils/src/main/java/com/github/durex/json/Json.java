package com.github.durex.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Json {

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  static {
    jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    jsonMapper.registerModule(new JavaTimeModule());
  }

  public static <T> T read(final String json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final byte[] json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final URL json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final File json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final InputStream json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final Reader json, final Class<T> tClass) throws IOException {
    return jsonMapper.readValue(json, tClass);
  }

  public static <T> T read(final String json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T read(final byte[] json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T read(final URL json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T read(final File json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T read(final InputStream json, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T read(final Reader json, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static String toString(final Object object) throws IOException {
    return jsonMapper.writeValueAsString(object);
  }

  public static byte[] toBytes(final Object object) throws IOException {
    return jsonMapper.writeValueAsBytes(object);
  }

  public static void write(final Object object, final File json) throws IOException {
    jsonMapper.writeValue(json, object);
  }

  public static void writeWithPretty(final Object object, final File json) throws IOException {
    jsonMapper.writerWithDefaultPrettyPrinter().writeValue(json, object);
  }

  public static void write(final Object object, final Writer writer) throws IOException {
    jsonMapper.writeValue(writer, object);
  }

  public static void write(Object object, final OutputStream json) throws IOException {
    jsonMapper.writeValue(json, object);
  }

  public static <T> T findNode(final String json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(final byte[] json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(final URL json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(final File json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(final InputStream json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(final Reader json, final String jsonPath, final Class<T> tClass)
      throws IOException {
    return jsonMapper.treeToValue(jsonMapper.readTree(json).at(jsonPath), tClass);
  }

  public static <T> T findNode(
      final String json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }

  public static <T> T findNode(
      final byte[] json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }

  public static <T> T findNode(
      final InputStream json, final String jsonPath, final TypeReference<T> typeRef)
      throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }

  public static <T> T findNode(
      final URL json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }

  public static <T> T findNode(
      final File json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }

  public static <T> T findNode(
      final Reader json, final String jsonPath, final TypeReference<T> typeRef) throws IOException {
    return jsonMapper.readValue(
        jsonMapper.treeAsTokens(jsonMapper.readTree(json).at(jsonPath)), typeRef);
  }
}
