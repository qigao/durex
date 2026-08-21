package com.github.durex.shared.spring.interceptor;

import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public final class NullCheckerAspect {

  @Around(
      "@annotation(com.github.durex.shared.annotation.NullChecker) || "
          + "@within(com.github.durex.shared.annotation.NullChecker)")
  public Object nullChecker(ProceedingJoinPoint joinPoint) throws Throwable {
    var result = joinPoint.proceed();
    if (isEmpty(result)) {
      throw new ApiException("No Data Returned", ErrorCode.OPERATION_FAILED);
    }
    return result;
  }

  private static boolean isEmpty(Object value) {
    if (value == null) {
      return true;
    }
    if (value instanceof CharSequence sequence) {
      return sequence.isEmpty();
    }
    if (value instanceof Collection<?> collection) {
      return collection.isEmpty();
    }
    if (value instanceof Map<?, ?> map) {
      return map.isEmpty();
    }
    if (value instanceof Optional<?> optional) {
      return optional.isEmpty();
    }
    return value.getClass().isArray() && Array.getLength(value) == 0;
  }
}
