package com.github.durex.service.api.impl;

import com.github.durex.service.api.ServiceG;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceGImpl implements ServiceG {
  @Override
  public void doWork() {
    log.info(
        "Interface and impl. Included via @Produces as impl. Decorated via @Alternative hack.");
  }
}
