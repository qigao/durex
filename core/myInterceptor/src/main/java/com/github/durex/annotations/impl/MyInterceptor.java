package com.github.durex.annotations.impl;

import com.github.durex.annotations.MyInterceptorAnnotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Interceptor
@MyInterceptorAnnotation
@Priority(Interceptor.Priority.APPLICATION + 1)
public class MyInterceptor {

  public static final Class<MyInterceptorAnnotation> ANNOTATION_CLASS =
      MyInterceptorAnnotation.class;

  @AroundInvoke
  Object myInvocation(InvocationContext context) throws Exception {
    final var method = context.getMethod();
    log.info("Invoking method: {}", method);
    getAnnotationInfo(method);
    var result = context.proceed();
    log.info("Invoked method result: {}", result);
    if (!"reactiveTest".equals(method.getName())) {
      return result;
    }

    var reactive = (Flux<String>) result;
    reactive.subscribe(x -> log.info("reactive: {}", x));
    Optional.of(Arrays.stream(context.getParameters()))
        .ifPresent(
            stream ->
                stream.forEach(
                    object ->
                        log.info(
                            "Invoked method param type: [{}], value: [{}]",
                            object.getClass().getName(),
                            object)));
    return result;
  }

  private void getAnnotationInfo(Method method) {
    log.info("INTERCEPTOR class:{}", method.getDeclaringClass().getSimpleName());
    var methodName = method.getName();
    log.info("INTERCEPTOR methodName:{}", methodName);
    var params = method.getParameterTypes();
    log.info("INTERCEPTOR params:{}", (Object[]) params);
    var annotation = method.getAnnotation(ANNOTATION_CLASS);
    var key = annotation.key();
    log.info("INTERCEPTOR annotation key:{}", key);
    var value = annotation.value();
    log.info("INTERCEPTOR annotation value:{}", value);
    var returnType = method.getGenericReturnType().getTypeName();
    log.info("INTERCEPTOR return: {}", returnType);
  }
}
