package com.github.durex.redisson.executor;

import java.io.Serializable;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTask implements Callable<String>, Serializable {

  @Override
  public String call() throws Exception {
    log.info("AsyncTask executed");
    return "hello";
  }
}
