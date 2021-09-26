package com.github.durex.pojo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockUtils {
  public static final int HTTP_PORT = 8090;
  public static final String HTTP_HOST = "http://localhost";

  public static URL getMockURL(String testUrl) throws IOException {
    return new URL(String.format("%s:%d%s", HTTP_HOST, HTTP_PORT, testUrl));
  }

  public static Path getMockDirectory(String fileUrl) {
    var fullDirectory = String.format("src/test/resources/__files%s", fileUrl);
    return Paths.get(fullDirectory);
  }
}
