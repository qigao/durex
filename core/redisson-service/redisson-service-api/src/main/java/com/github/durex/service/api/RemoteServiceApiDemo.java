package com.github.durex.service.api;

import static com.github.durex.messaging.api.enums.RemoteServiceEnum.ASYNC;
import static com.github.durex.messaging.api.enums.RemoteServiceEnum.REACTIVE;
import static com.github.durex.messaging.api.enums.RemoteServiceEnum.RXJAVA;
import static com.github.durex.messaging.api.enums.RemoteServiceEnum.SYNC;

import com.github.durex.messaging.api.annotation.RemoteServiceApi;

public interface RemoteServiceApiDemo {

  @RemoteServiceApi(serviceType = REACTIVE)
  String executeMe();

  @RemoteServiceApi(serviceType = REACTIVE)
  String executeMe2();

  @RemoteServiceApi(serviceType = SYNC)
  String anotherExecute();

  @RemoteServiceApi(serviceType = SYNC)
  String anotherExecute2();

  @RemoteServiceApi(serviceType = ASYNC)
  String test(String name, int a);

  @RemoteServiceApi(serviceType = ASYNC)
  String test2(String name, int a);

  @RemoteServiceApi(serviceType = RXJAVA)
  String anotherText(String value);

  @RemoteServiceApi(serviceType = RXJAVA)
  String anotherTet2(String value);
}
