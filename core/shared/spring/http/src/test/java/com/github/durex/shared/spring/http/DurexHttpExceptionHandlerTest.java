package com.github.durex.shared.spring.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DurexHttpExceptionHandlerTest {
  private final DurexHttpExceptionHandler handler = new DurexHttpExceptionHandler();

  @Test
  void mapsEntityNotFoundTo404() {
    assertEquals(
        HttpStatus.NOT_FOUND,
        handler.handleApiException(new ApiException("missing", ErrorCode.ENTITY_NOT_FOUND)).getStatusCode());
  }

  @Test
  void mapsEmptyParameterTo400() {
    assertEquals(
        HttpStatus.BAD_REQUEST,
        handler.handleApiException(new ApiException("empty", ErrorCode.EMPTY_PARAM)).getStatusCode());
  }

  @Test
  void mapsInvalidValueTo400() {
    assertEquals(
        HttpStatus.BAD_REQUEST,
        handler.handleApiException(new ApiException("invalid", ErrorCode.VALUE_ERROR)).getStatusCode());
  }

  @Test
  void keepsUnknownFailureAt500() {
    assertEquals(
        HttpStatus.INTERNAL_SERVER_ERROR,
        handler.handleApiException(new ApiException("unknown", ErrorCode.UNKNOWN_ERROR)).getStatusCode());
  }
}
