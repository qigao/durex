package com.github.durex.shared.exceptions;

import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runtime exception carrying Durex's structured API error information.
 *
 * <p>The two-argument constructor records the immediate caller so {@link #getErrorResponse()} can
 * expose diagnostic context together with the supplied {@link ErrorCode}. The message-only
 * constructor is available when no structured error code is known.
 */
public class ApiException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Human-readable error message returned to the API error response. */
  private String message;

  /** Structured error code associated with this exception, when supplied. */
  private ErrorCode errorCode;

  /** Fully qualified class and method that created the structured exception, when captured. */
  private String caller;

  /**
   * Creates an exception with a human-readable message and no explicit structured error code.
   *
   * @param message error message
   */
  public ApiException(String message) {
    super(message);
    this.message = message;
  }

  /**
   * Creates an exception with a message and structured error code and captures the immediate caller.
   *
   * @param message error message
   * @param errorCode structured error code
   */
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

  /**
   * Builds the structured response representation of this exception.
   *
   * <p>A fresh timestamp and error identifier are generated for each call.
   *
   * @return structured error response containing this exception's message, code, and caller
   */
  public ErrorResponse getErrorResponse() {
    return new ErrorResponse()
        .withMessage(message)
        .withErrorCode(errorCode)
        .withCaller(caller)
        .withTimestamp(LocalDateTime.now())
        .withErrorId(UUID.randomUUID());
  }
}
