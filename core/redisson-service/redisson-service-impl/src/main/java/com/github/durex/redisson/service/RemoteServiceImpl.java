package com.github.durex.redisson.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteServiceImpl implements RemoteServiceApi {

  @SneakyThrows
  @Override
  public String executeMe() {
    log.info("received remote service request from client");
    return "executed";
  }
}
