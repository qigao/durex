package com.github.durex.shared.utils;

import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.experimental.UtilityClass;

import static com.github.durex.shared.exceptions.model.ErrorCode.NOTHING_FAILED;

@UtilityClass
public class Helper {

  public Optional<String> makeOptional(String str) {
    if (str == null || str.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(str);
  }

  public ErrorResponse okResponse() {
    return new ErrorResponse()
        .withErrorId(UUID.randomUUID())
        .withMessage("OK")
        .withTimestamp(LocalDateTime.now())
        .withErrorCode(NOTHING_FAILED);
  }
}
