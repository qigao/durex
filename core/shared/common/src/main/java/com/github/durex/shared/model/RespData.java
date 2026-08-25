package com.github.durex.shared.model;

import com.github.durex.shared.exceptions.model.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Generic Durex HTTP response envelope.
 *
 * @param <T> successful result type
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor(staticName = "of")
public class RespData<T> {
  /** Successful response value, or {@code null} when the response contains only an error. */
  private T result;

  /** Structured error information, when present. */
  private ErrorResponse error;

  /** Creates an empty response envelope for data binding or incremental population. */
  public RespData() {}
}
