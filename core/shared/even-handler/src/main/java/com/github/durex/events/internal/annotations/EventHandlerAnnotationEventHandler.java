package com.github.durex.events.internal.annotations;

import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventPriority;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import lombok.Getter;

/** Default @EventHandler annotation handler. */
public class EventHandlerAnnotationEventHandler implements AnnotationEventHandler {

  /** The annotation class that will be used to handle events with this handler. */
  @Getter private final Class<? extends Annotation> annotation = EventHandler.class;

  /**
   * Obtain {@link EventPriority} from reflection method.
   *
   * @param method Reflected method to find the EventPriority.
   * @return EventPriority of the event method.
   */
  @Override
  public EventPriority getEventPriority(Method method) {
    EventHandler handler = method.getAnnotation(EventHandler.class);
    return handler.priority();
  }

  /**
   * Obtain 'ignoreCancelled' value from reflection method.
   *
   * @param method Reflected method to get the 'ignoreCancelled' value.
   * @return 'true' when ignoreCancelled was enabled.
   */
  @Override
  public boolean shouldIgnoreCancellable(Method method) {
    EventHandler handler = method.getAnnotation(EventHandler.class);
    return handler.ignoreCancelled();
  }
}
