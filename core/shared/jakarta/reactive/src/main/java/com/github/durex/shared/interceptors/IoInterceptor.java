package com.github.durex.shared.interceptors;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class IoInterceptor implements ReaderInterceptor, WriterInterceptor {
  @Override
  public Object aroundReadFrom(ReaderInterceptorContext context)
      throws IOException, WebApplicationException {
    InputStream is = context.getInputStream();
    String body =
        new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    log.info("body: {}", body);
    context.setInputStream(
        new ByteArrayInputStream(
            (body + " message added in server reader interceptor").getBytes()));

    var payload = context.proceed();
    log.info("after reading: {}", payload);
    return payload;
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context)
      throws IOException, WebApplicationException {
    log.info("before writing: {}", context.getEntity().toString());
    context.proceed();
    log.info("after writing: {}", context.getEntity().toString());
  }
}
