package com.github.durex.service;

import com.github.durex.annotations.MyInterceptorAnnotation;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ServiceA {
  @MyInterceptorAnnotation(key = "ServiceA", value = "ServiceA-for-demo")
  public void doWork() {
    log.info("Just the class, no interface. Included by annotations.");
  }
}
