package com.github.durex.service.api;

import com.github.durex.annotation.MyInterceptorAnnotation;

public interface ServiceE {
  @MyInterceptorAnnotation
  void doWork();
}
