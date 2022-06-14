package com.github.durex.redisson.service.api;

import org.redisson.api.annotation.RRemoteReactive;
import reactor.core.publisher.Mono;

@RRemoteReactive(RemoteServiceApi.class)
public interface RemoteServiceReactiveApi {

  Mono<String> executeMe();
}
