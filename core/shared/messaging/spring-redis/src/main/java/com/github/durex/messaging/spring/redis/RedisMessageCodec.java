package com.github.durex.messaging.spring.redis;

import java.lang.reflect.Type;

/**
 * Converts Durex Redis message values between Java objects and their string payload representation.
 *
 * <p>Decoding receives a {@link Type}, rather than only a {@link Class}, so custom codecs can
 * preserve parameterized listener types such as lists and maps.
 */
public interface RedisMessageCodec {
  /**
   * Encodes a Java value for Redis transport.
   *
   * @param value value returned by an outgoing method
   * @return serialized payload
   * @throws Exception when the value cannot be encoded
   */
  String encode(Object value) throws Exception;

  /**
   * Decodes a Redis payload to the listener's declared target type.
   *
   * @param payload serialized Redis payload
   * @param targetType listener parameter type, including generic type information when available
   * @return decoded Java value
   * @throws Exception when the payload cannot be decoded to the requested type
   */
  Object decode(String payload, Type targetType) throws Exception;
}
