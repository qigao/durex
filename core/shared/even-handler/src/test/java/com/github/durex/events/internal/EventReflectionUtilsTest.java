package com.github.durex.events.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.exceptions.EventHandlerNotFoundException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/** This class will test the EventRefection behavior with valid and invalid event handlers. */
class EventReflectionUtilsTest {

  @Test
  void getEventFromMethod_shouldReturnEventClassFromMethod_whenProvidedMethodWasWellFormed()
      throws NoSuchMethodException {
    Method handledFineMethod =
        WrongTestListener.class.getMethod("fineEventMethod", TestEvent.class);
    Class<? extends Event> eventClass = EventReflectionUtils.getEventFromMethod(handledFineMethod);

    // Assert equals
    assertEquals(TestEvent.class, eventClass);
  }

  @Test
  void
      getEventFromMethod_shouldThrowEventHandlerNotFoundException_whenProvidedMethodHasNoEventParam()
          throws NoSuchMethodException {
    Method handledWrongMethod = WrongTestListener.class.getMethod("wrongEventMethod");

    // This call should throw an EventHandlerNotFoundException
    assertThrows(
        EventHandlerNotFoundException.class,
        () -> EventReflectionUtils.getEventFromMethod(handledWrongMethod));
  }

  @Test
  void
      getEventFromMethod_shouldThrowEventHandlerNotFoundException_whenProvidedMethodHasParamButWasNotEvent()
          throws NoSuchMethodException {
    Method handledWrongMethod =
        WrongTestListener.class.getMethod("wrongEventMethodType", String.class);

    // This call should throw an EventHandlerNotFoundException
    assertThrows(
        EventHandlerNotFoundException.class,
        () -> EventReflectionUtils.getEventFromMethod(handledWrongMethod));
  }

  /**
   * This event listener will be used to prove the EventReflection behavior with invalid event
   * handlers.
   */
  public static class WrongTestListener implements EventListener {
    @EventHandler
    public void wrongEventMethod() {
      // This should be excluded from the event method list because they don't have a event type.
    }

    @EventHandler
    public void wrongEventMethodType(String string) {
      // This should be excluded from the event method list because they don't have a event type.
    }

    @EventHandler
    public void fineEventMethod(TestEvent event) {
      // This should be included in the event method list.
    }

    public void nonEventMethod() {
      // This should be excluded from the event method list because they don't have @EventHandler
      // annotation.
    }
  }

  /** This testing event is used to create a valid event handler. */
  public static class TestEvent extends Event {}
}
