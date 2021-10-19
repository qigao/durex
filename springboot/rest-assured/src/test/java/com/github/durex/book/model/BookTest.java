package com.github.durex.book.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.book.support.MockData;
import org.junit.jupiter.api.Test;

class BookTest {
  @Test
  void testEqualsBook() {
    var book1 = MockData.givenBooks();
    var book2 = MockData.givenBooks();
    var book3 = MockData.buildBookRequest();
    assertEquals(book1, book1);
    Book nullBook = null;
    assertNotEquals(book1, nullBook);
    assertNotEquals(book1, book3);
    assertTrue(book1.equals(book2) && book2.equals(book1));
    assertEquals(book1.hashCode(), book2.hashCode());
  }
}
