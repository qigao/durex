package com.github.durex.support;

import io.quarkus.test.common.*;
import org.junit.*;
import org.testcontainers.containers.*;

import java.util.*;

@SuppressWarnings("rawtypes")
public class MockedRedis implements QuarkusTestResourceLifecycleManager {
  private GenericContainer redis;

  @Override
  public Map<String, String> start() {
    redis = new GenericContainer("redis:latest").withExposedPorts(6379);
    redis.start();
    String address = redis.getHost();
    Integer port = redis.getFirstMappedPort();
    String hosts = String.format("redis://%s:%d", address, port);
    System.setProperty("quarkus.redis.hosts", hosts);
    return Collections.emptyMap();
  }

  @Override
  public void stop() {
    redis.stop();
  }
}
