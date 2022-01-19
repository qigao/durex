package com.github.durex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Text {
  String data;
  Integer size;
  String style;
  String name;
  Integer hOffset;
  Integer vOffset;
  String alignment;
  String onMouseUp;
}
