package com.github.durex.service.api.impl;

import com.github.durex.annotation.MapData;
import com.github.durex.annotation.MyInterceptorAnnotation;
import com.github.durex.service.api.ServiceC;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ServiceCImpl implements ServiceC {
  @MyInterceptorAnnotation(key = "ServiceC", value = "ServiceCImpl-for-demo")
  @Override
  public void doWork() {
    log.info("Interface and impl. Included via annotation. Interceptor annotation on impl.");
  }

  @MyInterceptorAnnotation(key = "ServiceC", value = "ServiceCImpl-for-test", types = MapData.class)
  public void test(String name) {
    log.info("test: " + name);
  }
}
