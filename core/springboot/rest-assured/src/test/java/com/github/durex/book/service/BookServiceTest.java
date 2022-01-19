package com.github.durex.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.durex.book.exception.BookNotFoundException;
import com.github.durex.book.support.MockData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookServiceTest {
  private static BookService bookService;

  @BeforeEach
  void setUp() {
    bookService = new BookService();
  }

  @AfterEach
  void tearDown() {
    bookService = null;
  }

  @Test
  @DisplayName("Get 1 book when BookService has no books")
  void testGetAllBooksReturnZero() {
    var bookList = bookService.getAllBooks(1);
    assertEquals(0, bookList.size());
  }

  @Test
  @DisplayName("Get 1 book when BookService has books")
  void testGetAllBooks() {
    var bookRequest = MockData.buildBookRequest();
    bookService.createNewBook(bookRequest);
    bookService.createNewBook(bookRequest);
    var bookList = bookService.getAllBooks(1);
    assertEquals(1, bookList.size());
  }

  @Test
  @DisplayName("Get book not exists")
  void testGetBookById() {
    var bookRequest = MockData.buildBookRequest();
    bookService.createNewBook(bookRequest);
    assertThrows(BookNotFoundException.class, () -> bookService.getBookById(0L));
  }
}
