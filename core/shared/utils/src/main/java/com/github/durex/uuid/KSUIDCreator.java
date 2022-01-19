package com.github.durex.uuid;

import com.github.f4b6a3.ksuid.KsuidFactory;

class KSUIDCreator implements Creator {
  public String generate() {
    return KsuidFactory.newInstance().create().toString();
  }
}
