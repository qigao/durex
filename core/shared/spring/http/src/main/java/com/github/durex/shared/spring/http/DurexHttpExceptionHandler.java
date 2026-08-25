package com.github.durex.shared.spring.http;

import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import com.github.durex.shared.model.RespData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class DurexHttpExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<RespData<Void>> handleApiException(ApiException exception) {
    ErrorResponse error = exception.getErrorResponse();
    return ResponseEntity.status(statusFor(error.getErrorCode())).body(RespData.of(null, error));
  }

  private static HttpStatus statusFor(ErrorCode errorCode) {
    return errorCode == ErrorCode.ENTITY_NOT_FOUND
        ? HttpStatus.NOT_FOUND
        : HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
