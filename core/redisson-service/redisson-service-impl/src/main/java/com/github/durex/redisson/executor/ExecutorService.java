package com.github.durex.redisson.executor;

import java.io.Serializable;
import java.util.concurrent.Callable;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

public class ExecutorService {
  public static class RunnableTask implements Runnable, Serializable {

    @RInject RedissonClient redisson;

    @Override
    public void run() {
      RMap<String, String> map = redisson.getMap("myMap");
      map.put("5", "11");
    }
  }

  public static class CallableTask implements Callable<String>, Serializable {

    @RInject RedissonClient redisson;

    @Override
    public String call() throws Exception {
      RMap<String, String> map = redisson.getMap("myMap");
      map.put("1", "2");
      return map.get("3");
    }
  }
}
