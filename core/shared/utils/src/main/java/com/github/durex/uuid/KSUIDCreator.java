package com.github.durex.uuid;

import com.github.f4b6a3.ksuid.KsuidCreator;

class KSUIDCreator implements Creator {
  public String generate() {
    return KsuidCreator.getKsuidNs().toString();
  }
}
