package com.github.durex.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Csv {
  private static final CsvMapper csvMapper = new CsvMapper();
  private static CsvSchema readerSchema = CsvSchema.emptySchema();
  private static CsvSchema writeSchema = CsvSchema.emptySchema();

  static {
    csvMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    csvMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    csvMapper.enable(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS);
    csvMapper.enable(CsvParser.Feature.TRIM_SPACES);
    csvMapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);
    csvMapper.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
    csvMapper.registerModule(new JavaTimeModule());
  }

  public static <T> T read(final String csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> T read(final byte[] csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> T read(final URL csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> T read(final File csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> T read(final InputStream csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> T read(final Reader csv, final Class<T> tClass) throws IOException {
    return csvMapper.readerFor(tClass).with(buildReaderSchema()).readValue(csv);
  }

  public static <T> List<T> readToList(final String csv, final Class<T> tClass) throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> List<T> readToList(final byte[] csv, final Class<T> tClass) throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> List<T> readToList(final URL csv, final Class<T> tClass) throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> List<T> readToList(final File csv, final Class<T> tClass) throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> List<T> readToList(final InputStream csv, final Class<T> tClass)
      throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> List<T> readToList(final Reader csv, final Class<T> tClass) throws IOException {
    MappingIterator<T> it = csvMapper.readerFor(tClass).with(buildReaderSchema()).readValues(csv);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> byte[] toBytes(Object object, Class<T> tClass) throws IOException {
    return csvMapper.writerFor(tClass).with(buildWriterSchema(tClass)).writeValueAsBytes(object);
  }

  public static <T> String toString(Object object, Class<T> tClass) throws IOException {
    return csvMapper.writerFor(tClass).with(buildWriterSchema(tClass)).writeValueAsString(object);
  }

  public static <T> void write(Object object, Class<T> tClass, Writer writer) throws IOException {
    csvMapper.writerFor(tClass).with(buildWriterSchema(tClass)).writeValue(writer, object);
  }

  public static <T> void write(Object object, Class<T> tClass, final OutputStream writer)
      throws IOException {
    csvMapper.writerFor(tClass).with(buildWriterSchema(tClass)).writeValue(writer, object);
  }

  public static <T> void write(Object object, Class<T> tClass, final File writer)
      throws IOException {
    csvMapper.writerFor(tClass).with(buildWriterSchema(tClass)).writeValue(writer, object);
  }

  public static void applyReaderSchema(CsvSchema csvSchema) {
    readerSchema = csvSchema.withHeader();
  }

  private static CsvSchema buildReaderSchema() {
    return readerSchema;
  }

  public static void applyWriterSchema(CsvSchema csvSchema) {
    writeSchema = csvSchema.withHeader();
  }

  private static <T> CsvSchema buildWriterSchema(Class<T> tClass) {
    CsvSchema.Builder tmpSchema = writeSchema.rebuild();
    Iterator<CsvSchema.Column> iterator = csvMapper.typedSchemaFor(tClass).iterator();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
        .forEach(tmpSchema::addColumn);
    return tmpSchema.build();
  }
}
