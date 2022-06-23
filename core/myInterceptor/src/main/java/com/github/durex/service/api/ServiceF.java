package com.github.durex.service.api;

import com.github.durex.annotations.MyInterceptorAnnotation;

public interface ServiceF {
  @MyInterceptorAnnotation
  void doWork();
}
