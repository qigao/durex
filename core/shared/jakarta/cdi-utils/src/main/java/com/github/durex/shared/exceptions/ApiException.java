package com.github.durex.shared.exceptions;

import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ApiException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 1L;

  private String message;
  private ErrorCode errorCode;
  private String caller;

  public ApiException(String message) {
    super(message);
    this.message = message;
  }

  public ApiException(String message, ErrorCode errorCode) {
    this(message);
    AtomicReference<String> callerMethod = new AtomicReference<>();
    AtomicReference<String> callerClass = new AtomicReference<>();
    StackWalker.getInstance()
        .walk(frames -> frames.skip(1).findFirst())
        .ifPresent(
            frame -> {
              callerMethod.set(frame.getMethodName());
              callerClass.set(frame.getClassName());
            });
    this.caller = callerClass.get() + "." + callerMethod.get();
    this.errorCode = errorCode;
  }

  public ErrorResponse getErrorResponse() {
    return new ErrorResponse()
        .withMessage(message)
        .withErrorCode(errorCode)
        .withCaller(caller)
        .withTimestamp(LocalDateTime.now())
        .withErrorId(UUID.randomUUID());
  }
}
