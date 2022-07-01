package com.github.durex.uniqid.impl;

import com.github.durex.uniqid.IdApi;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.github.f4b6a3.ulid.UlidFactory;
import java.util.UUID;

public class UlidApi implements IdApi<Ulid> {
  @Override
  public Long toLong(Ulid id) {
    return id.getInstant().toEpochMilli();
  }

  @Override
  public Long toLong(String id) {
    return Ulid.getInstant(id).toEpochMilli();
  }

  @Override
  public Ulid getId() {
    return UlidFactory.newMonotonicInstance().create();
  }

  @Override
  public String toString() {
    return UlidCreator.getMonotonicUlid().toString();
  }

  @Override
  public String toString(Ulid id) {
    return id.toString();
  }

  @Override
  public Ulid from(String id) {
    return Ulid.from(id);
  }

  public Ulid from(UUID id) {
    return Ulid.from(id);
  }

  public UUID toUuid(Ulid id) {
    return id.toUuid();
  }

  public UUID toUuid(String id) {
    return Ulid.from(id).toUuid();
  }
}
