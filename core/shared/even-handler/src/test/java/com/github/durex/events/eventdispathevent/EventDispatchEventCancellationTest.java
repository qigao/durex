package com.github.durex.events.eventdispathevent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.events.EventManager;
import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventDispatchEvent;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.internal.ReflectedEventManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test will ensure that the EventDispatchEvent cancellation prevents the event from being
 * dispatched.
 */
class EventDispatchEventCancellationTest {

  private EventManager eventManager;

  @BeforeEach
  void registerEventManager() {
    eventManager = new ReflectedEventManager();
    eventManager.registerEvents(new TestListener());
  }

  @Test
  void eventDispatchEventCancellationIsPreventingEventCall() {
    TestEvent testEvent = new TestEvent("cancel-me");
    eventManager.callEvent(testEvent);
    assertFalse(testEvent.isCalled());
  }

  @Test
  void eventDispatchEventListeningIsNotPreventingEventCall() {
    TestEvent testEvent = new TestEvent("random-name-not-cancelling");
    eventManager.callEvent(testEvent);
    assertTrue(testEvent.isCalled());
  }

  /** This event listener proves that event handled methods will be called. */
  public static class TestListener implements EventListener {
    @EventHandler
    public void dispatchEvent(EventDispatchEvent event) {
      TestEvent testEvent = (TestEvent) event.getEvent();
      if (testEvent.getName().equalsIgnoreCase("cancel-me")) {
        event.setCancelled(true);
      }
    }

    @EventHandler
    public void updateFieldName(TestEvent event) {
      event.setCalled(true);
    }
  }

  /** This event is used to prove that event handler methods are called as expected. */
  @RequiredArgsConstructor
  public static class TestEvent extends Event {
    @Getter private final String name;
    @Getter @Setter private boolean called;
  }
}
