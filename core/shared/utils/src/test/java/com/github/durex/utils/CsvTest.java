package com.github.durex.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.durex.model.OnlineCourse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvTest {
  public static final String SINGLE_CSV = "src/test/resources/csv/single.csv";
  public static final String THREE_CSV = "src/test/resources/csv/three.csv";
  public static final CsvSchema CSV_SCHEMA = CsvSchema.emptySchema().withColumnSeparator(',');
  @TempDir Path tempDir;

  @Test
  void testReadCSVByString() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(Files.readString(path), OnlineCourse.class);
    assertEquals(290, result.getPrice());
    Csv.applyWriterSchema(CSV_SCHEMA);
    var strData = Csv.toString(result, OnlineCourse.class);
    assertEquals(Csv.read(strData, OnlineCourse.class), result);
  }

  @Test
  void testReadCSVByBytes() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    var reader = new FileInputStream(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(reader.readAllBytes(), OnlineCourse.class);
    assertEquals(290, result.getPrice());
    Csv.applyWriterSchema(CSV_SCHEMA);
    var bytes = Csv.toBytes(result, OnlineCourse.class);
    assertEquals(Csv.read(bytes, OnlineCourse.class), result);
  }

  @Test
  void testReadCSVByURL() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(path.toUri().toURL(), OnlineCourse.class);
    assertEquals(290, result.getPrice());
  }

  @Test
  void testReadCSVByFile() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(path.toFile(), OnlineCourse.class);
    assertEquals(290, result.getPrice());
  }

  @Test
  void testReadCSVByInputStream() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    InputStream input = new FileInputStream(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(input, OnlineCourse.class);
    assertEquals(290, result.getPrice());
  }

  @Test
  void testReadCSVByReader() throws IOException {
    Path path = Path.of(SINGLE_CSV);
    Reader input = new FileReader(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var result = Csv.read(input, OnlineCourse.class);
    assertEquals(290, result.getPrice());
  }

  @Test
  void testReadCSVByStringAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    var csvString = Files.readString(path);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(csvString, OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testReadCSVByBytesAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    InputStream input = new FileInputStream(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(input.readAllBytes(), OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testReadCSVByURLAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(path.toUri().toURL(), OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testReadCSVByFileAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(path.toFile(), OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testReadCSVByInputStreamAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    InputStream input = new FileInputStream(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(input, OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testReadCSVByReaderAsList() throws IOException {
    Path path = Path.of(THREE_CSV);
    Reader input = new FileReader(path.toString());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var onlineCourses = Csv.readToList(input, OnlineCourse.class);
    assertThat(onlineCourses, contains(givenBookA(), givenBookB(), givenBookC()));
  }

  @Test
  void testWriteCsvToWriter() throws IOException {
    var onlineCourses = givenBookA();
    StringWriter writer = new StringWriter();
    Csv.applyWriterSchema(CSV_SCHEMA);
    Csv.write(onlineCourses, OnlineCourse.class, writer);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var objs = Csv.read(writer.toString(), OnlineCourse.class);
    assertEquals(onlineCourses, objs);
  }

  @Test
  void testWriteCsvToOutputStream() throws IOException {
    var onlineCourses = givenBookA();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Csv.applyWriterSchema(CSV_SCHEMA);
    Csv.write(onlineCourses, OnlineCourse.class, outputStream);
    Csv.applyReaderSchema(CSV_SCHEMA);
    var objs = Csv.read(outputStream.toString(), OnlineCourse.class);
    assertEquals(onlineCourses, objs);
  }

  @Test
  void testWriteCsvToFile() throws IOException {
    var onlineCourses = givenBookA();
    Csv.applyWriterSchema(CSV_SCHEMA);
    Csv.write(onlineCourses, OnlineCourse.class, tempDir.resolve("test.csv").toFile());
    Csv.applyReaderSchema(CSV_SCHEMA);
    var objs = Csv.read(tempDir.resolve("test.csv").toFile(), OnlineCourse.class);
    assertEquals(onlineCourses, objs);
  }

  private OnlineCourse givenBookA() {
    return new OnlineCourse("REST With Spring", "Eugen Paraschiv", 290);
  }

  private OnlineCourse givenBookB() {
    return new OnlineCourse("Learn Spring Security", "Baeldung", 290);
  }

  private OnlineCourse givenBookC() {
    return new OnlineCourse("Complete Java MasterClass", "Udemy", 200);
  }
}
