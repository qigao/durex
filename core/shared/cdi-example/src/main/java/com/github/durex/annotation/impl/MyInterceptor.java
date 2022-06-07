package com.github.durex.annotation.impl;

import com.github.durex.annotation.MyInterceptorAnnotation;
import java.lang.reflect.Method;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@MyInterceptorAnnotation
public class MyInterceptor {

  public static final Class<MyInterceptorAnnotation> ANNOTATION_CLASS =
      MyInterceptorAnnotation.class;

  @AroundInvoke
  Object logInvocation(InvocationContext context) throws Exception {
    final var method = context.getMethod();
    log.info("Invoking method: {}", method);
    retrieveAnnotationProperties(method);
    return context.proceed();
  }

  private void retrieveAnnotationProperties(Method method) {
    log.info("INTERCEPTOR class:{}", method.getDeclaringClass().getSimpleName());
    var methodName = method.getName();
    log.info("INTERCEPTOR methodName:{}", methodName);
    var params = method.getParameterTypes();
    log.info("INTERCEPTOR params:{}", (Object[]) params);
    final MyInterceptorAnnotation annotation = method.getAnnotation(ANNOTATION_CLASS);
    var key = annotation.key();
    log.info("INTERCEPTOR key:{}", key);
    var value = annotation.value();
    log.info("INTERCEPTOR value:{}", value);
    var types = annotation.types();
    log.info("INTERCEPTOR types:{}", types);
  }
}
