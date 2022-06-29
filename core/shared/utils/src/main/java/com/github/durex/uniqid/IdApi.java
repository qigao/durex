package com.github.durex.uniqid;

public interface IdApi<T> {
  Long toLong(T id);

  Long toLong(String id);

  T getId();

  String toString();

  String toString(T id);

  T from(String id);
}
