package com.github.durex.redisson.service;

import com.github.durex.messaging.api.annotation.RemoteService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RemoteService
public class RemoteServiceImpl implements RemoteServiceApi {

  @Override
  public String executeMe() {
    log.info("received remote service request from client");
    return "executed";
  }
}
