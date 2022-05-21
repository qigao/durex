package com.github.durex.utils;

import static com.github.durex.model.WeekDay.MONDAY;
import static com.github.durex.support.MockUtils.getMockDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.model.Div;
import com.github.durex.model.Text;
import com.github.durex.model.User;
import com.github.durex.model.WeekDay;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonTest {
  public static final String USER_LIST_JSON = "/json/users.json";
  public static final String USER_JSON = "/json/user.json";
  public static final String TEST_JSON = "test.json";
  public static final String TITLE = "title1";
  public static final String WIDGET_JSON = "src/test/resources/json/widget.json";
  public static final String WIDGET_DIV = "/widget/div";
  public static final String WIDGET_TEXT = "/widget/text";
  public static final String WIDGET_TEXT_SIZE = "/widget/text/size";
  @TempDir Path tempDir;

  @Test
  void testFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = Json.read(path.toFile(), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = Json.read(path.toFile(), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = Json.read(Files.readAllBytes(path), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = Json.read(Files.readString(path), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = Json.read(Files.readString(path), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = Json.read(Files.readAllBytes(path), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
    var listToBytes = Json.toBytes(userList);
    assertTrue(listToBytes.length > 1024);
  }

  @Test
  void testOnlineFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var url = path.toUri().toURL();
    var user = Json.read(url, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testOnlineFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var url = path.toUri().toURL();
    var user = Json.read(url, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testObjectToString() throws IOException {
    User user = givenUser();
    String serialized = Json.toString(user);
    assertTrue(serialized.contains("1"));
    assertTrue(serialized.contains(TITLE));
  }

  @Test
  void testInputStreamToObject() throws IOException {
    InputStream input = new FileInputStream(getMockDirectory(USER_JSON).toString());
    var user = Json.read(input, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testInputStreamToObjectList() throws IOException {
    InputStream input = new FileInputStream(getMockDirectory(USER_LIST_JSON).toString());
    var user = Json.read(input, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    Reader reader = new FileReader(path.toString());
    var user = Json.read(reader, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testReaderToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = Json.read(reader, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObjectArray() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = Json.read(reader, User[].class);
    assertEquals(200, user.length);
  }

  @Test
  void testFileWriter() throws IOException {
    User user = givenUser();
    Json.write(user, tempDir.resolve(TEST_JSON).toFile());
    var obj = Json.read(tempDir.resolve(TEST_JSON).toFile(), User.class);
    assertThat(user, samePropertyValuesAs(obj));
  }

  @Test
  void testFileWriterWithPretty() throws IOException {
    User user = givenUser();
    Json.writeWithPretty(user, tempDir.resolve(TEST_JSON).toFile());
    var obj = Json.read(tempDir.resolve(TEST_JSON).toFile(), User.class);
    assertThat(user, samePropertyValuesAs(obj));
  }

  @Test
  void testObjectToWriter() throws IOException {
    User user = givenUser();
    StringWriter writer = new StringWriter();
    Json.write(user, writer);
    assertTrue(writer.toString().contains(TITLE));
  }

  @Test
  void testObjectToOutputStream() throws IOException {
    User user = givenUser();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Json.write(user, outputStream);
    assertTrue(outputStream.toString().contains(TITLE));
  }

  @Test
  void whenSerializingJava8DateAndReadingValue() throws IOException {
    String stringDate = "\"2020-12-20\"";
    LocalDate result = Json.read(stringDate, LocalDate.class);
    assertThat(result.toString(), containsString("2020-12-20"));
  }

  @Test
  void whenDeserializingLocalDateTime() throws IOException {
    LocalDate now = LocalDate.now();
    String converted = Json.toString(now);
    LocalDate restored = Json.read(converted, LocalDate.class);
    assertThat(now, is(restored));
  }

  @Test
  void testEnumSerializingClass() throws IOException {
    String mondayString = Json.toString(MONDAY.name());
    assertThat("\"MONDAY\"", containsString(mondayString));
  }

  @Test
  void testEnumDeserializingClass() throws IOException {
    String mondayString = Json.toString(MONDAY.name());
    WeekDay day = Json.read(mondayString, WeekDay.class);
    assertEquals(MONDAY, day);
  }

  @Test
  void testJsonNodeByPath() throws IOException {
    String str = "{\"name\":\"peter\", \"age\":\"20\"}";
    Integer value = Json.findNode(str, "/age", Integer.class);
    assertEquals(20, value);
  }

  @Test
  void testJsonNodeByPathWithJsonFile() throws IOException {
    var path = Path.of(WIDGET_JSON);
    Integer value = Json.findNode(path.toFile(), WIDGET_TEXT_SIZE, Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonUrl() throws IOException {
    var path = Path.of(WIDGET_JSON);
    Integer value = Json.findNode(path.toUri().toURL(), WIDGET_TEXT_SIZE, Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonReader() throws IOException {
    var path = Path.of(WIDGET_JSON);
    Reader reader = new FileReader(path.toString());
    Integer value = Json.findNode(reader, WIDGET_TEXT_SIZE, Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonInputStream() throws IOException {
    var path = Path.of(WIDGET_JSON);
    InputStream input = new FileInputStream(path.toString());
    Integer value = Json.findNode(input, WIDGET_TEXT_SIZE, Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonBytes() throws IOException {
    var path = Path.of(WIDGET_JSON);
    Integer value = Json.findNode(Files.readAllBytes(path), WIDGET_TEXT_SIZE, Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonString() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value = Json.findNode(Files.readString(path), WIDGET_TEXT, Text.class);
    assertEquals(36, value.getSize());
    assertEquals("text1", value.getName());
  }

  @Test
  void testJsonNodeByPathWithJsonReaderTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value =
        Json.findNode(
            new FileReader(path.toString()), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonInputStreamTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value =
        Json.findNode(
            new FileInputStream(path.toString()), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonUrlTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value = Json.findNode(path.toUri().toURL(), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonStringTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value =
        Json.findNode(Files.readString(path), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonBytesTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value =
        Json.findNode(Files.readAllBytes(path), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonFileTypedReferenceList() throws IOException {
    var path = Path.of(WIDGET_JSON);
    var value = Json.findNode(path.toFile(), WIDGET_DIV, new TypeReference<List<Div>>() {});
    MatcherAssert.assertThat(value, hasSize(3));
    MatcherAssert.assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  private User givenUser() {
    return User.builder().id(1).userId(2).title(TITLE).completed(true).build();
  }
}
