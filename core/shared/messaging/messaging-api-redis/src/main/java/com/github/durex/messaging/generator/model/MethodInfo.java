package com.github.durex.messaging.generator.model;

import com.github.durex.messaging.api.enums.RemoteServiceEnum;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
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
public class MethodInfo {
  private Name methodName;
  private TypeMirror returnType;
  private String params;
  private RemoteServiceEnum serviceType;
}
