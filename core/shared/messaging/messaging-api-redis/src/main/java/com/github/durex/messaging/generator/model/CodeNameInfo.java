package com.github.durex.messaging.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeNameInfo {
  private String packageName;
  private String className;
  private String simpleClassName;
  private String simpleParamType;
  private String methodName;
  private String paramType;
}
