package com.github.durex.redisson.producer;

import com.github.durex.redisson.executor.AsyncTask;
import io.quarkus.runtime.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RExecutorFuture;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.WorkerOptions;

@Slf4j
@Startup
@ApplicationScoped
public class AsyncTaskProducer {
  @Inject RedissonClient redissonClient;

  @Produces
  @ApplicationScoped
  public RExecutorFuture<String> getExecutorFuture() {
    log.info("AsyncTask created");
    RScheduledExecutorService t = redissonClient.getExecutorService("test");
    t.registerWorkers(WorkerOptions.defaults());
    return t.submit(new AsyncTask());
  }
}
