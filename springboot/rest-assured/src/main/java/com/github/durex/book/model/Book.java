package com.github.durex.book.model;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    if (o == null) return false;
    if (this.getClass() == o.getClass()) {
      return Objects.equals(id, ((Book) o).id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
