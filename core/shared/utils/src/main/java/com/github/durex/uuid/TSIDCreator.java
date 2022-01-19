package com.github.durex.uuid;

import com.github.f4b6a3.tsid.TsidFactory;

class TSIDCreator implements Creator {
  public String generate() {
    return TsidFactory.newInstance1024().create().toString();
  }
}
