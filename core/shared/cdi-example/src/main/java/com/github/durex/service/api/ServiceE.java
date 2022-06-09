package com.github.durex.service.api;

import com.github.durex.annotations.MyInterceptorAnnotation;

public interface ServiceE {
  @MyInterceptorAnnotation
  void doWork();
}
