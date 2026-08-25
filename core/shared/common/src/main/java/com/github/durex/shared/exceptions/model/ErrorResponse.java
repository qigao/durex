package com.github.durex.shared.exceptions.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

/** Structured error payload used by Durex API responses. */
@With
@Data
@AllArgsConstructor
public class ErrorResponse implements Serializable {
  /** Correlation identifier generated for this error occurrence. */
  private UUID errorId;

  /** Human-readable description of the failure. */
  private String message;

  /** Optional class-and-method location that originated the failure. */
  private String caller;

  /** Stable structured classification of the failure. */
  private ErrorCode errorCode;

  /** Time at which the error response was created. */
  private LocalDateTime timestamp;

  /** Creates an empty error response for incremental population or data binding. */
  public ErrorResponse() {}
}
