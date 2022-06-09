package com.github.durex.service.api.impl;

import com.github.durex.annotations.MyInterceptorAnnotation;
import com.github.durex.service.api.ServiceG;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Alternative
@ApplicationScoped
@Priority(1)
@Slf4j
public class ServiceGDecorator implements ServiceG {
  /** Must specifically request impl, otherwise would recursively inject itself. */
  @Inject ServiceGImpl delegate;

  @Override
  @MyInterceptorAnnotation(key = "ServiceG", value = "ServiceGDecorator-for-demo")
  public void doWork() {
    log.info("DECORATOR HACK!");
    delegate.doWork();
  }
}
