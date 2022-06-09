package com.github.durex.shared.annotations.impl;

import com.github.durex.shared.annotations.AuthKey;
import javax.annotation.Priority;
import javax.interceptor.Interceptor;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@AuthKey
@Slf4j
@Provider
@Priority(Interceptor.Priority.APPLICATION + 1)
public class AuthKeyFilter implements ContainerRequestFilter {
  public static final String UNAUTHORIZED_STR = "Unauthorized";
  @Context UriInfo info;

  @Override
  public void filter(ContainerRequestContext context) {
    var encryptedData = context.getHeaders().getFirst("X-AUTH-KEY");
    log.info("encryptedData: {}", encryptedData);
  }
}
