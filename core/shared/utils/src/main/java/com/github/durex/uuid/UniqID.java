package com.github.durex.uuid;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UniqID {
  private final Map<IDType, Supplier<Creator>> map = new EnumMap<>(IDType.class);

  static {
    map.put(IDType.UUID, UUIDCreator::new);
    map.put(IDType.KSUID, KSUIDCreator::new);
    map.put(IDType.TSID, TSIDCreator::new);
    map.put(IDType.ULID, ULIDCreator::new);
  }

  public String getId(IDType type) {
    return map.get(type).get().generate();
  }

  public String getId() {
    return map.get(IDType.ULID).get().generate();
  }
}
