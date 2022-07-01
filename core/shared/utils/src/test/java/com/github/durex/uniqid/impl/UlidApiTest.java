package com.github.durex.uniqid.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.uniqid.UniqID;
import org.junit.jupiter.api.Test;

class UlidApiTest {

  @Test
  void testToUuid() {
    var ulid = UniqID.ULID.getId();
    var uuid = UniqID.ULID.toUuid(ulid);
    var uuidFromStr = UniqID.ULID.toUuid(ulid.toString());
    var fromUUID = UniqID.ULID.from(uuid);
    final int isSameID = uuid.compareTo(fromUUID.toUuid());
    assertEquals(0, isSameID);
    assertEquals(uuidFromStr.toString(), uuid.toString());
  }

  @Test
  void testToLong() {
    var ulid = UniqID.ULID.getId();
    var ulidStr = UniqID.ULID.toString(ulid);
    assertEquals(UniqID.ULID.toLong(ulid), UniqID.ULID.toLong(ulidStr));
  }

  @Test
  void testFromStr() {
    var id = UniqID.ULID.toString();
    var idLong = UniqID.ULID.toLong(id);
    var ulid = UniqID.ULID.from(id);
    assertEquals(idLong, UniqID.ULID.toLong(ulid));
  }
}
