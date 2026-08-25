package com.github.durex.shared.exceptions;

import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Runtime exception carrying one stable Durex structured API error occurrence.
 *
 * <p>The structured response is created with the exception and remains stable for the lifetime of
 * that exception. No implementation class or method name is exposed in the public error payload.
 */
public class ApiException extends RuntimeException implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final ErrorResponse errorResponse;

  /**
   * Creates an exception with a human-readable message and no explicit structured error code.
   *
   * @param message error message
   */
  public ApiException(String message) {
    this(message, null);
  }

  /**
   * Creates an exception with a message and structured error code.
   *
   * @param message error message
   * @param errorCode structured error code, or {@code null} when no classification is known
   */
  public ApiException(String message, ErrorCode errorCode) {
    super(message);
    this.errorResponse =
        new ErrorResponse(UUID.randomUUID(), message, errorCode, LocalDateTime.now());
  }

  /**
   * Returns the structured response representation of this error occurrence.
   *
   * @return stable structured error response for this exception
   */
  public ErrorResponse getErrorResponse() {
    return errorResponse;
  }
}
