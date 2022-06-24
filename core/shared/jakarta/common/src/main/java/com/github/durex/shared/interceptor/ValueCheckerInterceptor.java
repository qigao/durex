package com.github.durex.shared.interceptor;

import com.github.durex.shared.annotation.ValueChecker;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import java.util.Objects;
import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@ValueChecker
@Priority(Interceptor.Priority.APPLICATION + 2)
public class ValueCheckerInterceptor {
  private static final Class<ValueChecker> ANNOTATION_CLASS = ValueChecker.class;

  @AroundInvoke
  Object valueChecker(InvocationContext context) throws Exception {
    var method = context.getMethod();
    log.info("Invoking method: {}", method);
    var annotation = method.getAnnotation(ANNOTATION_CLASS);
    var valueType = annotation.type();
    log.info("INTERCEPTOR annotation valueType:{}", valueType);
    var exception = annotation.exception();
    var errorMsg = annotation.message();
    var value = annotation.value();
    log.info("INTERCEPTOR annotation value:{}", value);
    var returnType = method.getGenericReturnType().getTypeName();
    log.info("INTERCEPTOR return: {}", returnType);
    var typeClass = Class.forName(returnType);
    if (!Objects.equals(valueType, typeClass)) {
      throw new ApiException("Return Value Check Error", ErrorCode.VALUE_ERROR);
    }
    var oResult = context.proceed();
    if (Objects.equals(oResult.toString(), value)) {
      throw exception.getConstructor(String.class).newInstance(errorMsg);
    }
    return oResult;
  }
}
