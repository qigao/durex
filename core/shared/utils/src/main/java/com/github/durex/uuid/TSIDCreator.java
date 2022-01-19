package com.github.durex.uuid;

import com.github.f4b6a3.tsid.TsidCreator;

class TSIDCreator implements Creator {
  public String generate() {
    return TsidCreator.getTsid1024().toString();
  }
}
