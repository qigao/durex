package com.github.durex.events.internal;

import com.github.durex.events.api.Event;
import com.github.durex.events.exceptions.EventHandlerNotFoundException;
import java.lang.reflect.Method;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** This class manages internally event's reflection operations. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventReflectionUtils {

  /**
   * Obtain event class from reflected method.
   *
   * @param method Reflected method to find event class.
   * @return reflected event class from specified method.
   */
  @SuppressWarnings("all")
  public static Class<? extends Event> getEventFromMethod(Method method) {
    if (method.getParameterCount() != 1) {
      throw new EventHandlerNotFoundException(
          "Handled method doesn't have a event parameter or have more than one.");
    }

    Class<?> parameter = method.getParameterTypes()[0];
    if (!Event.class.isAssignableFrom(parameter)) {
      throw new EventHandlerNotFoundException(
          "Handled method event parameter aren't a valid event.");
    }

    return (Class<? extends Event>) parameter;
  }
}
