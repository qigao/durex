package com.github.durex.uuid;

import com.github.durex.utils.IDType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UniqID {
  static final Map<IDType, Supplier<Creator>> map = new EnumMap<>(IDType.class);

  static {
    map.put(IDType.UUID, UUIDCreator::new);
    map.put(IDType.KSUID, KSUIDCreator::new);
    map.put(IDType.TSID, TSIDCreator::new);
    map.put(IDType.ULID, ULIDCreator::new);
  }

  public static String getId(IDType type) {
    return map.get(type).get().generate();
  }

  public static String getId() {
    return map.get(IDType.ULID).get().generate();
  }
}
