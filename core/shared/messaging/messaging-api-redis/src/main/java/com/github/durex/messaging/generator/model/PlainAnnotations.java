package com.github.durex.messaging.generator.model;

import com.github.durex.messaging.api.model.CodecEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PlainAnnotations extends Annotations {
  private CodecEnum codec;
  private String value;
  private String subscriber;
}
