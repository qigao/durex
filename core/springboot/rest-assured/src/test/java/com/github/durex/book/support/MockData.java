package com.github.durex.book.support;

import com.github.durex.book.model.Book;
import com.github.durex.book.model.BookRequest;

public class MockData {
  public static Book givenBooks() {
    return Book.builder()
        .id(42L)
        .isbn("42")
        .title("REST Assured With Spring Boot")
        .author("Duke")
        .build();
  }

  public static BookRequest buildBookRequest() {
    return BookRequest.builder()
        .isbn("42")
        .title("REST Assured With Spring Boot")
        .author("Duke")
        .build();
  }
}
