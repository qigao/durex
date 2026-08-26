package com.github.durex.reference.publication;

import com.github.durex.messaging.annotation.Outgoing;
import com.github.durex.messaging.annotation.RedisStreamListener;
import com.github.durex.messaging.annotation.RedisStreamOutgoing;
import com.github.durex.messaging.spring.redis.RedisMessageCodec;
import com.github.durex.messaging.spring.redis.RedisStreamFailureDisposition;
import com.github.durex.messaging.spring.redis.RedisStreamListenerFailure;
import com.github.durex.messaging.spring.redis.RedisStreamListenerFailureHandler;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import com.github.durex.shared.exceptions.model.ErrorResponse;
import com.github.durex.shared.model.RespData;
import com.github.durex.shared.spring.http.DurexHttpExceptionHandler;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Compile-only compatibility fixture for the public Durex API promised by the initial Maven surface.
 * It intentionally depends only on the staged Maven artifacts, never on Durex project sources.
 */
public final class PublicApiBaseline {
  private final DurexHttpExceptionHandler exceptionHandler = new DurexHttpExceptionHandler();

  public RespData<String> response(String value) {
    return RespData.of(value, null);
  }

  public String responseResult(RespData<String> response) {
    return response.result();
  }

  public ErrorResponse responseError(RespData<?> response) {
    return response.error();
  }

  public ApiException apiException(String message) {
    return new ApiException(message, ErrorCode.UNKNOWN_ERROR);
  }

  public ErrorResponse errorResponse(ApiException exception) {
    return exception.getErrorResponse();
  }

  public ErrorResponse errorResponse(
      UUID errorId, String message, ErrorCode errorCode, LocalDateTime timestamp) {
    return new ErrorResponse(errorId, message, errorCode, timestamp);
  }

  public UUID errorId(ErrorResponse error) {
    return error.errorId();
  }

  public String errorMessage(ErrorResponse error) {
    return error.message();
  }

  public ErrorCode responseErrorCode(ErrorResponse error) {
    return error.errorCode();
  }

  public LocalDateTime errorTimestamp(ErrorResponse error) {
    return error.timestamp();
  }

  public ErrorCode errorCode() {
    return ErrorCode.UNKNOWN_ERROR;
  }

  public DurexHttpExceptionHandler exceptionHandler() {
    return exceptionHandler;
  }

  public RedisMessageCodec codec(RedisMessageCodec codec) {
    return codec;
  }

  public RedisStreamListenerFailureHandler failureHandler() {
    return RedisStreamListenerFailureHandler.keepPending();
  }

  public RedisStreamListenerFailure listenerFailure(RedisStreamListenerFailure failure) {
    return failure;
  }

  public RedisStreamFailureDisposition keepPending() {
    return RedisStreamFailureDisposition.KEEP_PENDING;
  }

  @Outgoing("public.api.pubsub")
  public String outgoing(String value) {
    return value;
  }

  @RedisStreamOutgoing("public.api.stream")
  public String streamOutgoing(String value) {
    return value;
  }

  @RedisStreamListener(
      stream = "public.api.stream",
      group = "public-api",
      consumer = "baseline",
      autoAck = false)
  public void streamListener(String value) {}
}
