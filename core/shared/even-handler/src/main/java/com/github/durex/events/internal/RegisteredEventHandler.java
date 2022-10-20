package com.github.durex.events.internal;

import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.api.EventPriority;
import com.github.durex.events.internal.annotations.AnnotationEventHandler;
import com.github.durex.events.internal.annotations.HandledMethod;
import java.lang.reflect.Method;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** This class maps an @EventHandler method registered in an EventManager. */
@Slf4j
public class RegisteredEventHandler {

  private Method method;
  private AnnotationEventHandler handler;

  @Getter(AccessLevel.PACKAGE)
  private Class<? extends Event> event;

  @Getter private EventListener listener;

  @Getter private EventPriority priority;
  @Getter private boolean ignoreCancelled;
  @Getter private String name;

  public RegisteredEventHandler(EventListener listener, HandledMethod handledMethod) {
    this.listener = listener;

    method = handledMethod.getMethod();
    handler = handledMethod.getHandler();

    name = method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
    event = EventReflectionUtils.getEventFromMethod(method);
    priority = handler.getEventPriority(method);
    ignoreCancelled = handler.shouldIgnoreCancellable(method);
  }

  /**
   * Execute the register event.
   *
   * @param event Called event instance.
   */
  public void invoke(Event event) {
    try {
      method.invoke(listener, event);
    } catch (Exception e) {
      log.error("[EventManager] Error launching '" + name + "' event handler.");
      e.printStackTrace();
    }
  }
}
