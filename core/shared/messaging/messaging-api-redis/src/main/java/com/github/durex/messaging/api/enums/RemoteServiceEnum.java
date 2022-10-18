package com.github.durex.messaging.api.enums;

public enum RemoteServiceEnum {
  SYNC("sync"),
  ASYNC("async"),
  REACTIVE("reactive"),
  RXJAVA("rxjava");
  private final String serviceType;

  RemoteServiceEnum(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getServiceType() {
    return serviceType;
  }
}
