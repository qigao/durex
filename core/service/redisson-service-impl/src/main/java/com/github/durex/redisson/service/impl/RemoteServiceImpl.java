package com.github.durex.redisson.service.impl;

import com.github.durex.redisson.service.api.RemoteServiceApi;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteServiceImpl implements RemoteServiceApi {

  @SneakyThrows
  @Override
  public String executeMe() {
    return "executed";
  }
}
