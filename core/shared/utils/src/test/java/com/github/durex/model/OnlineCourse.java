package com.github.durex.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"Title", "Author", "Price"})
public class OnlineCourse {
  @JsonProperty("Title")
  private String title;

  @JsonProperty("Author")
  private String author;

  @JsonProperty("Price")
  private int price;
}
