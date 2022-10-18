package com.github.durex.events.internal.annotations;

import java.lang.reflect.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents a event handler method. In case some method got more than one handling
 * annotation it will have one HandledMethod for each handler.
 */
@Getter
@RequiredArgsConstructor
public class HandledMethod {
  private final Method method;
  private final AnnotationEventHandler handler;
}
