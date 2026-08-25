package com.github.durex.shared.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.shared.exceptions.model.ErrorResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class HttpModelSurfaceTest {

  @Test
  void responseTypesAreExplicitImmutableRecords() {
    assertTrue(ErrorResponse.class.isRecord());
    assertTrue(RespData.class.isRecord());
  }

  @Test
  void legacyCallerBuilderAndMutatorMethodsAreNotPublicSurface() {
    Set<String> errorMethods =
        Arrays.stream(ErrorResponse.class.getMethods())
            .map(method -> method.getName())
            .collect(Collectors.toSet());
    Set<String> responseMethods =
        Arrays.stream(RespData.class.getMethods())
            .map(method -> method.getName())
            .collect(Collectors.toSet());

    assertFalse(errorMethods.contains("getCaller"));
    assertFalse(errorMethods.contains("setCaller"));
    assertFalse(errorMethods.contains("withCaller"));
    assertFalse(errorMethods.contains("setErrorId"));
    assertFalse(errorMethods.contains("withErrorId"));
    assertFalse(responseMethods.contains("builder"));
    assertFalse(responseMethods.contains("setResult"));
    assertFalse(responseMethods.contains("setError"));
  }
}
