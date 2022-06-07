package com.github.durex.service.api;

import com.github.durex.annotation.MyInterceptorAnnotation;

public interface ServiceF {
  @MyInterceptorAnnotation
  void doWork();
}
