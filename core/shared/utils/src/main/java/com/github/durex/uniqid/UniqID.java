package com.github.durex.uniqid;

import com.github.durex.uniqid.impl.TsidApi;
import com.github.durex.uniqid.impl.UlidApi;
import com.github.durex.uniqid.impl.UuidApi;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UniqID {
  public final UuidApi UUID = new UuidApi();
  public final UlidApi ULID = new UlidApi();

  public final TsidApi TSID = new TsidApi();

  public String getId() {
    return ULID.toString();
  }
}
