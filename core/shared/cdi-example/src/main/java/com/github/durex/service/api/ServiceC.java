package com.github.durex.service.api;

import java.util.List;
import reactor.core.publisher.Mono;

public interface ServiceC {
  void doWork();

  List<String> test(String name);

  Mono<String> reactiveTest(String name);
}
