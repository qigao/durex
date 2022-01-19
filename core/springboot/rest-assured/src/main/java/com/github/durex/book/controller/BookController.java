package com.github.durex.book.controller;

import com.github.durex.book.model.Book;
import com.github.durex.book.model.BookRequest;
import com.github.durex.book.service.BookService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public List<Book> getAllBooks(@RequestParam(value = "amount", defaultValue = "500") int amount) {
    return bookService.getAllBooks(amount);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Book> getBookById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(bookService.getBookById(id));
  }

  @PostMapping
  public ResponseEntity<Void> createNewBook(
      @Valid @RequestBody BookRequest bookRequest, UriComponentsBuilder uriComponentsBuilder) {

    Long bookId = bookService.createNewBook(bookRequest);

    var uriComponents = uriComponentsBuilder.path("/api/books/{id}").buildAndExpand(bookId);
    var headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());

    return new ResponseEntity<>(headers, HttpStatus.CREATED);
  }
}
