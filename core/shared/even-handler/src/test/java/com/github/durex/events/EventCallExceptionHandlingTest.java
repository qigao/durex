package com.github.durex.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.internal.ReflectedEventManager;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This class will test the behavior of an event handler that throws an exception. */
class EventCallExceptionHandlingTest {

  private EventManager eventManager;

  @BeforeEach
  void registerEventManager() {
    eventManager = new ReflectedEventManager();
  }

  @Test
  void callingEventHandlerThatThrowExceptions_shouldNotStopTheEventExecutionFlow() {
    // Register event listeners
    eventManager.registerEvents(new TestListener());
    eventManager.registerEvents(new TestThatThrowsExceptionsListener());

    // Create an event and call it
    TestEvent event = new TestEvent();
    eventManager.callEvent(event);

    // Assert expected behavior
    assertEquals("Changed", event.getTestName());
  }

  /**
   * This event listener will be used to prove that in case any other event handler throws an
   * exception this will be executed anyway.
   */
  public static class TestListener implements EventListener {
    @EventHandler
    public void updateFieldName(TestEvent event) {
      event.setTestName("Changed");
    }
  }

  /**
   * This event listener will be used to prove the behavior or an event handler that throws an
   * exception.
   */
  public static class TestThatThrowsExceptionsListener implements EventListener {
    @EventHandler
    public void exceptionThrower(TestEvent event) {
      throw new RuntimeException("Testing what happens when something goes wrong!");
    }
  }

  /** This event will be used to test the exception throwing behavior. */
  public static class TestEvent extends Event {
    @Getter @Setter private String testName;
  }
}
