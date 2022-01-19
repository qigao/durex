package com.github.durex.book.service;

import com.github.durex.book.exception.BookNotFoundException;
import com.github.durex.book.model.Book;
import com.github.durex.book.model.BookRequest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookService {

  private final List<Book> bookStore = new ArrayList<>();

  public Long createNewBook(BookRequest bookRequest) {
    Book book =
        Book.builder()
            .isbn(bookRequest.getIsbn())
            .author(bookRequest.getAuthor())
            .title(bookRequest.getTitle())
            .id(new SecureRandom().nextLong())
            .build();

    bookStore.add(book);

    return book.getId();
  }

  public List<Book> getAllBooks(int amount) {

    if (this.bookStore.size() > amount) {
      return this.bookStore.subList(0, amount);
    }

    return this.bookStore;
  }

  public Book getBookById(Long id) {
    return this.bookStore.stream()
        .filter(book -> book.getId().equals(id))
        .findFirst()
        .orElseThrow(
            () -> new BookNotFoundException(String.format("Book with id: '%s' not found", id)));
  }
}
