package com.github.durex.messaging.test;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusMain
public class RedissonServiceDaemon implements QuarkusApplication {

  @Override
  public int run(String... args) throws Exception {
    log.info("starting redisson remote service daemon...");
    Quarkus.waitForExit();
    return 0;
  }
}
