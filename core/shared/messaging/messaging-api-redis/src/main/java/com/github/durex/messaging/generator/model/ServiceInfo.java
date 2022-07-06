package com.github.durex.messaging.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo {
  private String packageName;
  private String className;
  private String simpleInterfaceName;
  private String interfaceName;
}
