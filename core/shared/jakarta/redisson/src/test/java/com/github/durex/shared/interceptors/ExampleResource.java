package com.github.durex.shared.interceptors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.github.durex.shared.annotations.Cached;

@Path("/")
public class ExampleResource {
  @GET
  @Cached(key = "key")
  public String getHello() {
    return "Hello";
  }
}
