package com.github.durex.shared.interceptor;

import java.util.Collection;
import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.github.durex.shared.annotation.NullValueChecker;
import com.github.durex.shared.exceptions.ApiException;
import com.github.durex.shared.exceptions.model.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
@Interceptor
@NullValueChecker
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 1)
public class NullValueCheckerInterceptor {

  @AroundInvoke
  Object nullCheckerInvoke(InvocationContext context) throws Exception {
    var oResult = context.proceed();
    if (oResult instanceof Collection<?> && CollectionUtils.isEmpty((Collection<?>) oResult)) {
      throw new ApiException("Result Not Found", ErrorCode.OPERATION_FAILED);
    }
    if(ObjectUtils.isEmpty(oResult))
    {
      throw new ApiException("Result Not Found", ErrorCode.OPERATION_FAILED);
    }
    return oResult;
  }
}
