package com.github.durex.events;

import com.github.durex.events.internal.ReflectedEventManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Main entry point of the ReflectedEventHandler library. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectedEventHandler {

  /**
   * Creates a new instance of EventManager to use it around applications.
   *
   * @return EventManager instance ready to be used.
   */
  public static EventManager createEventManager() {
    return new ReflectedEventManager();
  }
}
