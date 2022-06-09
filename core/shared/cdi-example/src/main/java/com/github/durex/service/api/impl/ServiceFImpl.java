package com.github.durex.service.api.impl;

import com.github.durex.annotations.MyInterceptorAnnotation;
import com.github.durex.service.api.ServiceF;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceFImpl implements ServiceF {
  @Override
  public void doWork() {
    log.info(
        "Interface and impl. Included via @Produces as impl. Interceptor annotations on interface.");
  }
}
