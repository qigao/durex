package com.github.durex.book.model;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class BookTest {
  @Test
  void testEqualBook() {
    EqualsVerifier.forClass(Book.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(Book.class).verify();
  }
}
