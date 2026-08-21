package com.github.durex.shared.support;

import static com.github.durex.shared.exceptions.model.ErrorCode.NOTHING_FAILED;

import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Helper {
  /**
   * @return a new {@link ErrorResponse} set to {@link ErrorCode#NOTHING_FAILED} and message to OK
   */
  public ErrorResponse okResponse() {
    return new ErrorResponse()
        .withErrorId(UUID.randomUUID())
        .withMessage("OK")
        .withTimestamp(LocalDateTime.now())
        .withErrorCode(NOTHING_FAILED);
  }
}
