package com.github.durex.shared.utils;

import static com.github.durex.shared.exceptions.model.ErrorCode.NOTHING_FAILED;

import com.github.durex.shared.api.RespData;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.experimental.UtilityClass;

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

  public RespData respOk() {
    return RespData.builder().error(okResponse()).build();
  }

  public RespData respError(ErrorResponse errorResp) {
    return RespData.builder().error(errorResp).build();
  }

  public RespData respData(Object data) {
    return RespData.builder().result(data).build();
  }

  public RespData respData(ErrorResponse errorResp, Object data) {
    return RespData.builder().result(data).error(errorResp).build();
  }
}
