package com.github.durex.messaging.generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Annotations {
  private String packageName;
  private String className;
  private String simpleClassName;
  private String simpleParamType;
  private String methodName;
  private String paramType;
}
