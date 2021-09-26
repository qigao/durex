package pl.jlabs.model;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Product {
  private String name;
  private double price;
  private Date creationDate;
  private Date lastUpdated;
}
