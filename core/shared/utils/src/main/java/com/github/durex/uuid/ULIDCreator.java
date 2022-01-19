package com.github.durex.uuid;

import com.github.f4b6a3.ulid.UlidFactory;

class ULIDCreator implements Creator {
  public String generate() {
    return UlidFactory.newInstance().create().toString();
  }
}
