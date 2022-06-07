package com.github.durex.service;

import com.github.durex.annotation.MyInterceptorAnnotation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceB {
  @MyInterceptorAnnotation
  public void doWork() {
    log.info("Just the class, no interface. Included via @Produces.");
  }
}
