package com.github.durex.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.json.model.Div;
import com.github.durex.json.model.Text;
import com.github.durex.json.model.User;
import com.github.durex.json.model.WeekDay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static com.github.durex.json.model.WeekDay.MONDAY;
import static com.github.durex.pojo.MockUtils.getMockDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerDeTest {
  public static final String USER_LIST_JSON = "/json/users.json";
  public static final String USER_JSON = "/json/user.json";
  public static final String TEST_JSON = "test.json";
  public static final String TITLE = "title1";
  @TempDir Path tempDir;

  @Test
  void testFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(path.toFile(), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(path.toFile(), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(Files.readAllBytes(path), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(Files.readString(path), User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(Files.readString(path), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(Files.readAllBytes(path), new TypeReference<List<User>>() {});
    assertEquals(200, userList.size());
    var listToBytes = SerDe.toBytes(userList);
    assertTrue(listToBytes.length > 1024);
  }

  @Test
  void testOnlineFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var url = path.toUri().toURL();
    var user = SerDe.toObject(url, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testOnlineFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var url = path.toUri().toURL();
    var user = SerDe.toObject(url, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testObjectToString() throws IOException {
    User user = givenUser();
    String serialized = SerDe.toString(user);
    assertTrue(serialized.contains("1"));
    assertTrue(serialized.contains(TITLE));
  }

  @Test
  void testInputStreamToObject() throws IOException {
    InputStream input = new FileInputStream(getMockDirectory(USER_JSON).toString());
    var user = SerDe.toObject(input, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testInputStreamToObjectList() throws IOException {
    InputStream input = new FileInputStream(getMockDirectory(USER_LIST_JSON).toString());
    var user = SerDe.toObject(input, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(reader, User.class);
    assertEquals(1, user.getId());
  }

  @Test
  void testReaderToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(reader, new TypeReference<List<User>>() {});
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObjectArray() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(reader, User[].class);
    assertEquals(200, user.length);
  }

  @Test
  void testFileWriter() throws IOException {
    User user = givenUser();
    SerDe.toFile(user, tempDir.resolve(TEST_JSON).toFile());
    assertTrue(tempDir.resolve(TEST_JSON).toFile().exists());
  }

  @Test
  void testFileWriterWithPretty() throws IOException {
    User user = givenUser();
    SerDe.toFileWithPretty(user, tempDir.resolve(TEST_JSON).toFile());
    assertTrue(tempDir.resolve(TEST_JSON).toFile().exists());
  }

  @Test
  void testObjectToWriter() throws IOException {
    User user = givenUser();
    StringWriter writer = new StringWriter();
    SerDe.toWriter(user, writer);
    assertTrue(writer.toString().contains(TITLE));
  }

  @Test
  void testObjectToOutputStream() throws IOException {
    User user = givenUser();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SerDe.toOutputStream(user, outputStream);
    assertTrue(outputStream.toString().contains(TITLE));
  }

  @Test
  void whenSerializingJava8DateAndReadingValue_thenCorrect() throws IOException {
    String stringDate = "\"2020-12-20\"";
    LocalDate result = SerDe.toObject(stringDate, LocalDate.class);
    assertThat(result.toString(), containsString("2020-12-20"));
  }

  @Test
  void whenDeserializingLocalDateTime_thenCorrect() throws IOException {
    LocalDate now = LocalDate.now();
    String converted = SerDe.toString(now);
    LocalDate restored = SerDe.toObject(converted, LocalDate.class);
    assertThat(now, is(restored));
  }

  @Test
  void testEnumSerializingClass() throws IOException {
    String mondayString = SerDe.toString(MONDAY.name());
    assertThat("\"MONDAY\"", containsString(mondayString));
  }

  @Test
  void testEnumDeserializingClass() throws IOException {
    String mondayString = SerDe.toString(MONDAY.name());
    WeekDay day = SerDe.toObject(mondayString, WeekDay.class);
    assertEquals(MONDAY, day);
  }

  @Test
  void testJsonNodeByPath() throws IOException {
    String str = "{\"name\":\"peter\", \"age\":\"20\"}";
    Integer value = SerDe.findJsonNode(str, "/age", Integer.class);
    assertEquals(20, value);
  }

  @Test
  void testJsonNodeByPathWithJsonFile() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    Integer value = SerDe.findJsonNode(path.toFile(), "/widget/text/size", Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonUrl() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    Integer value = SerDe.findJsonNode(path.toUri().toURL(), "/widget/text/size", Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonReader() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    Reader reader = new FileReader(path.toString());
    Integer value = SerDe.findJsonNode(reader, "/widget/text/size", Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonInputStream() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    InputStream input = new FileInputStream(path.toString());
    Integer value = SerDe.findJsonNode(input, "/widget/text/size", Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonBytes() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    Integer value =
        SerDe.findJsonNode(Files.readAllBytes(path), "/widget/text/size", Integer.class);
    assertEquals(36, value);
  }

  @Test
  void testJsonNodeByPathWithJsonString() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value = SerDe.findJsonNode(Files.readString(path), "/widget/text", Text.class);
    assertEquals(36, value.getSize());
    assertEquals("text1", value.getName());
  }

  @Test
  void testJsonNodeByPathWithJsonReaderTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value =
        SerDe.findJsonNode(
            new FileReader(path.toString()), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonInputStreamTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value =
        SerDe.findJsonNode(
            new FileInputStream(path.toString()), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonUrlTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value =
        SerDe.findJsonNode(path.toUri().toURL(), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonStringTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value =
        SerDe.findJsonNode(
            Files.readString(path), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonBytesTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value =
        SerDe.findJsonNode(
            Files.readAllBytes(path), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  @Test
  void testJsonNodeByPathWithJsonFileTypedReferenceList() throws IOException {
    var path = Paths.get("src/test/resources/json/widget.json");
    var value = SerDe.findJsonNode(path.toFile(), "/widget/div", new TypeReference<List<Div>>() {});
    assertThat(value, hasSize(3));
    assertThat(
        value,
        containsInAnyOrder(
            hasProperty("name", is("avatar01")),
            hasProperty("name", is("avatar02")),
            hasProperty("name", is("avatar03"))));
  }

  private User givenUser() {
    return User.builder().id(1).title(TITLE).build();
  }
}
