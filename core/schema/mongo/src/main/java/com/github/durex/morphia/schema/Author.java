package com.github.durex.morphia.schema;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity("Books")
public class Author {
  @Id private String id;
  private String firstName;
  private String lastName;
}
