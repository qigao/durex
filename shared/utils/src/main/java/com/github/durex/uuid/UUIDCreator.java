package com.github.durex.uuid;

import static com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedWithHash;

import com.github.f4b6a3.uuid.codec.StringCodec;

class UUIDCreator implements Creator {
  public String generate() {
    return StringCodec.INSTANCE.encode(getTimeOrderedWithHash());
  }
}
