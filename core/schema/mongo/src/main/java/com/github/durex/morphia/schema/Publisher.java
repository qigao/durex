package com.github.durex.morphia.schema;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity("Publishers")
public class Publisher {
  @Id private ObjectId id;
  private String name;
}
