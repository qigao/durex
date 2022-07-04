package com.github.durex.morphia.schema;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

@Setter
@Getter
@ToString
@Entity("employees")
@Indexes(@Index(options = @IndexOptions(name = "salary"), fields = @Field("salary")))
public class Employee {
  @Id private ObjectId id;
  private String name;
  private Integer age;
  @Reference private Employee manager;
  @Reference private List<Employee> directReports = new ArrayList<>();

  @Property("wage")
  private Double salary;

  Employee() {}

  public Employee(String name, Double salary) {
    this.name = name;
    this.salary = salary;
  }
}
