package com.github.durex.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

/** Helper */
@UtilityClass
public class Helper {
  public <T> Optional<T> toOptional(T value) {
    return Optional.ofNullable(value);
  }

  public <T> Optional<String> readResource(String path, Class<T> clazz) throws IOException {
    var inputStream = clazz.getResourceAsStream(path);
    if (inputStream == null) {
      return Optional.empty();
    }
    return Optional.of(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
  }
}
