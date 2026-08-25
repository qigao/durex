package com.github.durex.shared.spring.http;

import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import com.github.durex.shared.model.RespData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring MVC exception advice that maps {@link ApiException} to the Durex response envelope.
 *
 * <p>{@link ErrorCode#ENTITY_NOT_FOUND} is returned as HTTP 404. Other Durex API errors are
 * returned as HTTP 500.
 */
@RestControllerAdvice
public final class DurexHttpExceptionHandler {

  /** Creates the stateless Durex HTTP exception handler. */
  public DurexHttpExceptionHandler() {}

  /**
   * Converts a Durex API exception into an HTTP response containing structured error details.
   *
   * @param exception exception raised by application code
   * @return HTTP response with a {@code null} result and the exception's structured error payload
   */
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
