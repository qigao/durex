package com.github.durex.uniqid.impl;

import com.github.durex.uniqid.IdApi;
import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.tsid.TsidCreator;

public class TsidApi implements IdApi<Tsid> {
  @Override
  public Long toLong(Tsid id) {
    return id.toLong();
  }

  @Override
  public Long toLong(String id) {
    return Tsid.from(id).toLong();
  }

  @Override
  public Tsid getId() {
    return TsidCreator.getTsid1024();
  }

  @Override
  public String toString() {
    return TsidCreator.getTsid1024().toString();
  }

  @Override
  public String toString(Tsid id) {
    return id.toString();
  }

  @Override
  public Tsid from(String id) {
    return Tsid.from(id);
  }
}
