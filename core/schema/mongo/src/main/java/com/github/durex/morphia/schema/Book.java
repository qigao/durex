package com.github.durex.morphia.schema;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity("Books")
public class Book {
  @Id private String isbn;
  private String title;

  @Property("price")
  private double cost;

  private Publisher publisher;

  @Reference private ArrayList<Book> companionBooks = new ArrayList<>();

  @Reference private Author author;

  public Book(String isbn) {
    this.isbn = isbn;
  }

  public Book(String isbn, String title, double cost, Publisher publisher) {
    this.isbn = isbn;
    this.title = title;
    this.cost = cost;
    this.publisher = publisher;
  }

  public void addCompanionBook(Book companionBook) {
    companionBooks.add(companionBook);
  }
}
