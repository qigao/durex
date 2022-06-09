package com.github.durex.shared.annotations.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.annotation.Priority;
import javax.interceptor.Interceptor;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.github.durex.shared.annotations.Logged;
import lombok.extern.slf4j.Slf4j;

@Logged
@Provider
@Slf4j
@Priority(Interceptor.Priority.APPLICATION+2)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    log.info(
        "Request: {} {} {}",
        requestContext.getMethod(),
        requestContext.getUriInfo().getPath(),
        requestContext.getUriInfo().getQueryParameters().toString());
    log.info("Headers: {}", requestContext.getHeaders());
    if (requestContext.hasEntity()) {
      BufferedInputStream in = new BufferedInputStream(requestContext.getEntityStream());
      var payload = in.readAllBytes();
      log.info("Entity: {}", new String(payload, UTF_8));
      requestContext.setEntityStream(new ByteArrayInputStream(payload));
    }
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    log.info(
        "Response: {} {} {} {}",
        requestContext.getMethod(),
        requestContext.getUriInfo().getPath(),
        requestContext.getUriInfo().getQueryParameters(),
        responseContext.getStatus());
  }
}
