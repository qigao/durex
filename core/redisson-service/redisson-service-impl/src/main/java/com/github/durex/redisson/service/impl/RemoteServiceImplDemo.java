package com.github.durex.redisson.service.impl;

import com.github.durex.messaging.api.annotation.QuarkusDaemon;
import com.github.durex.messaging.api.annotation.RemoteService;
import com.github.durex.service.api.ReactiveRemoteServiceApiDemo;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RemoteService
@QuarkusDaemon
public class RemoteServiceImplDemo implements ReactiveRemoteServiceApiDemo {

  @Override
  public Mono<String> executeMe() {
    log.info("received remote service request from client");
    return Mono.just("executed");
  }

  @Override
  public Mono<String> executeMe2() {
    return Mono.just("2");
  }
}
