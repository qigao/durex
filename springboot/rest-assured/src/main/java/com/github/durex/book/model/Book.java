package com.github.durex.book.model;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.*;
import org.hibernate.Hibernate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Book {
  @Id private Long id;
  private String isbn;
  private String title;
  private String author;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    Book book = (Book) o;
    return Objects.equals(id, book.id);
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
