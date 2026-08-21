package com.github.durex.shared.exceptions.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@With
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse implements Serializable {
  private UUID errorId;
  private String message;
  private String caller;
  private ErrorCode errorCode;
  private LocalDateTime timestamp;
}
