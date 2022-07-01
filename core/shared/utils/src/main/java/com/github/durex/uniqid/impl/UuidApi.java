package com.github.durex.uniqid.impl;

import com.github.durex.uniqid.IdApi;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.codec.base.Base64UrlCodec;
import com.github.f4b6a3.uuid.util.UuidUtil;
import java.util.UUID;

public class UuidApi implements IdApi<UUID> {
  @Override
  public Long toLong(UUID id) {
    return id.timestamp();
  }

  @Override
  public Long toLong(String id) {
    return UuidUtil.getTimestamp(UuidCreator.fromString(id));
  }

  @Override
  public UUID getId() {
    return UuidCreator.getTimeBasedWithRandom();
  }

  @Override
  public String toString() {
    return UuidCreator.getTimeBasedWithRandom().toString();
  }

  @Override
  public String toString(UUID id) {
    return id.toString();
  }

  @Override
  public UUID from(String id) {
    return UuidCreator.fromString(id);
  }

  /**
   * convert an uuid to base-64-url, which include "A-Za-z0-9-_"
   *
   * @param id uuid
   * @return a base-64-url string
   */
  public String getUrl(UUID id) {
    return new Base64UrlCodec().encode(id);
  }

  public UUID from(Ulid id) {
    return id.toUuid();
  }
}
