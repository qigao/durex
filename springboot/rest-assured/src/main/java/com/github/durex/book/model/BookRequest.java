package com.github.durex.book.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookRequest {

  @NotEmpty private String title;

  @NotEmpty
  @Size(max = 20)
  private String isbn;

  @NotEmpty private String author;
}
