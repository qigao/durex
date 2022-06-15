package com.github.durex.service.api;

import java.util.List;
import reactor.core.publisher.Flux;

public interface ServiceC {
  void doWork();

  List<String> test(String name);

  Flux<String> reactiveTest(String name);
}
