package com.github.durex.book.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class BookTest {
  @Test
  void testEqualBook() {
    EqualsVerifier.simple().forClass(Book.class).verify();
  }
}
