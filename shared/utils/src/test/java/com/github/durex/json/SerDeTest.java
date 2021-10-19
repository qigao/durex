package com.github.durex.json;

import static com.github.durex.pojo.MockUtils.getMockDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.durex.json.model.User;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SerDeTest {
  public static final String USER_LIST_JSON = "/json/users.json";
  public static final String USER_JSON = "/json/user.json";
  public static final String TEST_JSON = "test.json";
  public static final String TITLE = "title1";
  @TempDir Path tempDir;

  @Test
  void testFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(User.class, path.toFile());
    assertEquals(1, user.getId());
  }

  @Test
  void testFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(new TypeReference<List<User>>() {}, path.toFile());
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(User.class, Files.readAllBytes(path));
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var user = SerDe.toObject(User.class, Files.readString(path));
    assertEquals(1, user.getId());
  }

  @Test
  void testStringToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(new TypeReference<List<User>>() {}, Files.readString(path));
    assertEquals(200, userList.size());
  }

  @Test
  void testByteToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var userList = SerDe.toObject(new TypeReference<List<User>>() {}, Files.readAllBytes(path));
    assertEquals(200, userList.size());
    var listToBytes = SerDe.toBytes(userList);
    assertTrue(listToBytes.length > 1024);
  }

  @Test
  void testOnlineFileToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    var url = path.toUri().toURL();
    var user = SerDe.toObject(User.class, url);
    assertEquals(1, user.getId());
  }

  @Test
  void testOnlineFileToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    var url = path.toUri().toURL();
    var user = SerDe.toObject(new TypeReference<List<User>>() {}, url);
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
    var user = SerDe.toObject(User.class, input);
    assertEquals(1, user.getId());
  }

  @Test
  void testInputStreamToObjectList() throws IOException {
    InputStream input = new FileInputStream(getMockDirectory(USER_LIST_JSON).toString());
    var user = SerDe.toObject(new TypeReference<List<User>>() {}, input);
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObject() throws IOException {
    var path = getMockDirectory(USER_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(User.class, reader);
    assertEquals(1, user.getId());
  }

  @Test
  void testReaderToObjectList() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(new TypeReference<List<User>>() {}, reader);
    assertEquals(200, user.size());
  }

  @Test
  void testReaderToObjectArray() throws IOException {
    var path = getMockDirectory(USER_LIST_JSON);
    Reader reader = new FileReader(path.toString());
    var user = SerDe.toObject(User[].class, reader);
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

  private User givenUser() {
    return User.builder().id(1).title(TITLE).build();
  }
}
