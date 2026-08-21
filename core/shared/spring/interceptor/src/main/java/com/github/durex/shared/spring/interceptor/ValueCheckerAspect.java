package com.github.durex.shared.spring.interceptor;

import com.github.durex.shared.annotation.ValueChecker;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import java.lang.reflect.Method;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public final class ValueCheckerAspect {

  @Around(
      "@annotation(com.github.durex.shared.annotation.ValueChecker) || "
          + "@within(com.github.durex.shared.annotation.ValueChecker)")
  public Object valueChecker(ProceedingJoinPoint joinPoint) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    ValueChecker annotation = method.getAnnotation(ValueChecker.class);
    if (annotation == null && joinPoint.getTarget() != null) {
      annotation = joinPoint.getTarget().getClass().getAnnotation(ValueChecker.class);
    }
    if (annotation == null) {
      return joinPoint.proceed();
    }

    if (!Objects.equals(annotation.type(), method.getReturnType())) {
      throw new ApiException("Return Value Check Error", ErrorCode.VALUE_ERROR);
    }

    Object result = joinPoint.proceed();
    if (Objects.equals(String.valueOf(result), annotation.value())) {
      throw annotation.exception().getConstructor(String.class).newInstance(annotation.message());
    }
    return result;
  }
}
