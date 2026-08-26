package com.github.durex.shared.exceptions;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.github.durex.shared.exceptions.model.ErrorCode;
import org.junit.jupiter.api.Test;

class ApiExceptionContractTest {

  @Test
  void oneExceptionKeepsOneStructuredErrorOccurrence() {
    var exception = new ApiException("invalid value", ErrorCode.VALUE_ERROR);

    var first = exception.getErrorResponse();
    var second = exception.getErrorResponse();

    assertSame(first, second);
  }
}
