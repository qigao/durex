package com.github.durex.uniqid.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.uniqid.UniqID;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class UuidApiTest {
  @Test
  void testToLong() {
    var uuid = UniqID.UUID.getId();
    var idStr = UniqID.UUID.toString(uuid);
    var idLong = UniqID.UUID.toLong(uuid);
    var strIDLong = UniqID.UUID.toLong(idStr);
    assertEquals(idLong, strIDLong);
  }

  @Test
  void testFromStr() {
    var id = UniqID.UUID.toString();
    var idLong = UniqID.UUID.toLong(id);
    var uuid = UniqID.UUID.from(id);
    assertEquals(idLong, UniqID.UUID.toLong(uuid));
  }

  @Test
  void testFromUlid() {
    var ulid = UniqID.ULID.getId();
    var uuid = UniqID.UUID.from(ulid);
    var fromUUID = UniqID.ULID.from(uuid);
    final int isSameID = uuid.compareTo(fromUUID.toUuid());
    assertEquals(0, isSameID);
  }

  @Test
  void testBase64Url() {
    var uuid = UniqID.UUID.getId();
    var url = UniqID.UUID.getUrl(uuid);
    assertThat(url, Matchers.not(CoreMatchers.containsString("/")));
  }
}
