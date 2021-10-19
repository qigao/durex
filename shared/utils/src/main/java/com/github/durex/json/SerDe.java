package com.github.durex.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    jsonMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    jsonMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
  }

  public static <T> T toObject(final Class<T> type, final String json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Class<T> type, final byte[] json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Class<T> type, final URL json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Class<T> type, final File json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Class<T> type, final InputStream json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final Class<T> type, final Reader json) throws IOException {
    return jsonMapper.readValue(json, type);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final String json)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final byte[] json)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final URL json) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final File json) throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final InputStream json)
      throws IOException {
    return jsonMapper.readValue(json, typeRef);
  }

  public static <T> T toObject(final TypeReference<T> typeRef, final Reader json)
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
}
