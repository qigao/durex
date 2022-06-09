package com.github.durex.service.api.impl;

import com.github.durex.annotations.MapData;
import com.github.durex.annotations.MyInterceptorAnnotation;
import com.github.durex.service.api.ServiceC;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@ApplicationScoped
@Slf4j
public class ServiceCImpl implements ServiceC {
  @MyInterceptorAnnotation(key = "ServiceC", value = "ServiceCImpl-for-demo")
  @Override
  public void doWork() {
    log.info("Interface and impl. Included via annotations. Interceptor annotations on impl.");
  }

  @MyInterceptorAnnotation(key = "ServiceC", value = "ServiceCImpl-for-test", types = MapData.class)
  public List<String> test(String name) {
    log.info("test: " + name);
    return List.of("test: ", name);
  }

  @MyInterceptorAnnotation(
      key = "ServiceC",
      value = "ServiceCImpl-for-reactive-test",
      types = MapData.class)
  public Mono<String> reactiveTest(String name) {
    log.info("reactive test: " + name);
    return Mono.just(name);
  }
}
