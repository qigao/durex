package com.github.durex.shared.filter;

import io.vertx.core.http.HttpServerRequest;
import java.io.IOException;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
  @Context UriInfo info;

  @Context HttpServerRequest request;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    final String method = requestContext.getMethod();
    final String path = info.getPath();
    final String address = request.remoteAddress().toString();
    log.info("Request {} {} from IP {}", method, path, address);
    log.info("Headers: {}", requestContext.getHeaders());
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    responseContext.getHeaders().add("X-Test", "Test response filter");
    log.info(
        "Response: {} {} {} {}",
        requestContext.getMethod(),
        requestContext.getUriInfo().getPath(),
        requestContext.getUriInfo().getQueryParameters(),
        responseContext.getStatus());
  }
}
