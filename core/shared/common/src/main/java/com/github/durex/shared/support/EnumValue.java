package com.github.durex.shared.support;

public class EnumValue<T extends Enum<T>> {
  Class<T> enumClass;

  public EnumValue(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  public Class<T> enumClass() {
    return enumClass;
  }
}
