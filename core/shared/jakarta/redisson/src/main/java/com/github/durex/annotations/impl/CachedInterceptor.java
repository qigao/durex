package com.github.durex.annotations.impl;

import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.github.durex.annotations.Cached;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

@Interceptor
@Priority(1000)
@Cached
@Slf4j
public class CachedInterceptor {
  public static final Class<Cached> ANNOTATION_CLASS = Cached.class;
  @Inject RedissonClient redissonClient;

  @AroundInvoke
  public Object execute(InvocationContext ctx) throws Exception {
    final var method = ctx.getMethod();
    log.info("Invoking method: {}", method);
    retrieveAnnotationProperties(method);
    return ctx.proceed();
  }

  private void retrieveAnnotationProperties(Method method) {
    log.info("INTERCEPTOR class:{}", method.getDeclaringClass().getSimpleName());
    var methodName = method.getName();
    log.info("INTERCEPTOR methodName:{}", methodName);
    var params = method.getParameterTypes();
    log.info("INTERCEPTOR params:{}", (Object[]) params);
    var annotation = method.getAnnotation(ANNOTATION_CLASS);
    var key = annotation.key();
    log.info("INTERCEPTOR key:{}", key);
    var value = annotation.value();
    log.info("INTERCEPTOR value:{}", value);
  }
}
