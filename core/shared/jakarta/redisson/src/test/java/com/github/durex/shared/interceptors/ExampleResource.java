package com.github.durex.shared.interceptors;

import com.github.durex.annotations.Cached;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class ExampleResource {
  @GET
  @Cached(key = "key")
  public String getHello() {
    return "Hello";
  }
}
