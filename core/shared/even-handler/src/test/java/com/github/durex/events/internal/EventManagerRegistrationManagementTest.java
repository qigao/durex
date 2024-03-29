package com.github.durex.events.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.events.EventManager;
import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This class proves the behavior of the EventManager event registering and event calling. */
class EventManagerRegistrationManagementTest {

  private EventManager eventManager;
  private EventManagement eventManagement;

  @BeforeEach
  void registerEventManager() throws NoSuchFieldException, IllegalAccessException {
    eventManager = new ReflectedEventManager();
    eventManagement = getEventManagement();
  }

  @Test
  void registerEvents_shouldRegisterMethodsWithEventHandler_whenTheyAreCorrectlyWritten()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          NoSuchFieldException {
    // Register events
    eventManager.registerEvents(new TestListener());

    // Obtain method listeners
    List<RegisteredEventHandler> registeredListeners =
        eventManagement.getEventHandlerFor(TestEvent.class);
    RegisteredEventHandler eventListener = registeredListeners.get(0);

    // Assert data
    assertEquals(1, registeredListeners.size());
    assertEquals(
        "com.github.durex.events.remote.EventManagerRegistrationManagementTest.TestListener#updateFieldName",
        eventListener.getName());
  }

  @Test
  void
      unregisterEvents_shouldUnregisterRegisteredEventHandlerMethods_whenProvidedListenerAreAlreadyRegistered()
          throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
              NoSuchFieldException {
    // Event to register & unregister
    TestListener testListener = new TestListener();

    // Register events
    eventManager.registerEvents(testListener);

    // Obtain method listeners
    List<RegisteredEventHandler> registeredListeners =
        eventManagement.getEventHandlerFor(TestEvent.class);
    RegisteredEventHandler eventListener = registeredListeners.get(0);

    // Assert registered data
    assertEquals(1, registeredListeners.size());
    assertEquals(
        "com.github.durex.events.remote.EventManagerRegistrationManagementTest.TestListener#updateFieldName",
        eventListener.getName());

    // Unregister and assert unregistered data
    eventManager.unregisterEvents(testListener);

    registeredListeners = eventManagement.getEventHandlerFor(TestEvent.class);
    assertEquals(0, registeredListeners.size());
  }

  private EventManagement getEventManagement() throws NoSuchFieldException, IllegalAccessException {
    Field field = eventManager.getClass().getDeclaredField("eventManagement");
    field.setAccessible(true);
    return (EventManagement) field.get(eventManager);
  }

  /**
   * This event listener proves that invalid event handlers will be ignore and valid ones will be
   * call.
   */
  public static class TestListener implements EventListener {
    @EventHandler
    public void updateFieldName(TestEvent event) {
      event.setTestName("Changed");
      // This should be included in the event method list.
    }

    public void nonEventMethod() {
      // This should be excluded from the event method list because they don't have the
      // @EventHandler annotation.
    }
  }

  /** This event is used to prove that valid event handlers are called as expected. */
  public static class TestEvent extends Event {
    @Getter @Setter private String testName;
  }
}
