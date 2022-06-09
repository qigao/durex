package com.github.durex.annotations.impl;

import static java.util.Arrays.stream;

import java.lang.reflect.Method;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.github.durex.annotations.MyInterceptorAnnotation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@MyInterceptorAnnotation
public class MyInterceptor {

  public static final Class<MyInterceptorAnnotation> ANNOTATION_CLASS =
      MyInterceptorAnnotation.class;

  @AroundInvoke
  Object myInvocation(InvocationContext context) throws Exception {
    final var method = context.getMethod();
    log.info("Invoking method: {}", method);
    getAnnotationInfo(method);
    var result= context.proceed();
    log.info("Invoked method result: {}", result);
    final var params = context.getParameters();
    stream(params).forEach(p -> log.info("Invoked method param: {}", p));
    return result;
  }

  private void getAnnotationInfo(Method method) {
    log.info("INTERCEPTOR class:{}", method.getDeclaringClass().getSimpleName());
    var methodName = method.getName();
    log.info("INTERCEPTOR methodName:{}", methodName);
    var params = method.getParameterTypes();
    log.info("INTERCEPTOR params:{}", (Object[]) params);
    final MyInterceptorAnnotation annotation = method.getAnnotation(ANNOTATION_CLASS);
    var key = annotation.key();
    log.info("INTERCEPTOR annotation key:{}", key);
    var value = annotation.value();
    log.info("INTERCEPTOR annotation value:{}", value);
    var types = annotation.types();
    log.info("INTERCEPTOR types:{}", types);
    var returnType = method.getGenericReturnType().getTypeName();
    log.info("INTERCEPTOR return: {}",returnType);

  }
}
