package com.github.durex.redisson.producer;

import com.github.durex.service.api.ReactiveRemoteServiceApiDemo;
import io.quarkus.runtime.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RemoteInvocationOptions;

@Slf4j
@Startup
@ApplicationScoped
public class ReactiveRemoteService {
  @Inject RedissonReactiveClient reactiveClient;

  @Produces
  @ApplicationScoped
  public ReactiveRemoteServiceApiDemo getRemoteService() {
    log.info("starting redisson remote service...");
    RemoteInvocationOptions options = RemoteInvocationOptions.defaults();
    return reactiveClient.getRemoteService().get(ReactiveRemoteServiceApiDemo.class, options);
  }
}
