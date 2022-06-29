package com.github.durex.uniqid.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.uniqid.UniqID;
import org.junit.jupiter.api.Test;

class TsidApiTest {
  @Test
  void testToLong() {
    var tsid = UniqID.TSID.getId();
    var idStr = UniqID.TSID.toString(tsid);
    var idLong = UniqID.TSID.toLong(tsid);
    var strIDLong = UniqID.TSID.toLong(idStr);
    assertEquals(idLong, strIDLong);
  }

  @Test
  void testFromStr() {
    var id = UniqID.TSID.toString();
    var idLong = UniqID.TSID.toLong(id);
    var tsid = UniqID.TSID.from(id);
    assertEquals(idLong, tsid.toLong());
  }
}
