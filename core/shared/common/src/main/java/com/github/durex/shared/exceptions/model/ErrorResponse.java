package com.github.durex.shared.exceptions.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable structured error payload used by Durex API responses.
 *
 * @param errorId correlation identifier for this error occurrence
 * @param message human-readable description of the failure
 * @param errorCode stable structured classification of the failure, when known
 * @param timestamp time at which this error occurrence was created
 */
public record ErrorResponse(
    UUID errorId, String message, ErrorCode errorCode, LocalDateTime timestamp)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
