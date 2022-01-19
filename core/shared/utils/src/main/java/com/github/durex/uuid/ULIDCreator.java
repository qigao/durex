package com.github.durex.uuid;

import com.github.f4b6a3.ulid.UlidCreator;

class ULIDCreator implements Creator {
  public String generate() {
    return UlidCreator.getMonotonicUlid().toString();
  }
}
