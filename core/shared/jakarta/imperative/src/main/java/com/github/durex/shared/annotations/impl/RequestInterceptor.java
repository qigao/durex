package com.github.durex.shared.annotations.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.durex.shared.annotations.Param;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.annotation.Priority;
import javax.interceptor.Interceptor;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import lombok.extern.slf4j.Slf4j;

@Param
@Provider
@Slf4j
@Priority(Interceptor.Priority.APPLICATION + 3)
public class RequestInterceptor implements ReaderInterceptor {

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext context)
      throws IOException, WebApplicationException {
    var payload = context.getInputStream().readAllBytes();
    log.info("requestBody:{}", new String(payload, UTF_8));
    context.setInputStream(new ByteArrayInputStream(payload));
    return context.proceed();
  }
}
