package com.github.durex.shared.model;

import com.github.durex.shared.exceptions.model.ErrorResponse;

/**
 * Immutable generic Durex HTTP response envelope.
 *
 * @param result successful response value, or {@code null} when the response contains only an error
 * @param error structured error information, when present
 * @param <T> successful result type
 */
public record RespData<T>(T result, ErrorResponse error) {

  /**
   * Creates a response envelope.
   *
   * @param result successful response value, or {@code null}
   * @param error structured error information, or {@code null}
   * @param <T> successful result type
   * @return immutable response envelope
   */
  public static <T> RespData<T> of(T result, ErrorResponse error) {
    return new RespData<>(result, error);
  }
}
