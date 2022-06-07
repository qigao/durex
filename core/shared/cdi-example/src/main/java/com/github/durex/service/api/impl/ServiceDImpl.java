package com.github.durex.service.api.impl;

import com.github.durex.annotation.MyInterceptorAnnotation;
import com.github.durex.service.api.ServiceD;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceDImpl implements ServiceD {
  @MyInterceptorAnnotation
  @Override
  public void doWork() {
    log.info(
        "Interface and impl. Included via @Produces as interface. Interceptor annotation on impl.");
  }
}
