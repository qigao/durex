package com.github.durex.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventCancellable;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.api.EventPriority;
import com.github.durex.events.internal.ReflectedEventManager;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This class tests the behavior of event execution flow when some of them were cancelled. */
class EventCancellableTest {

  private EventManager eventManager;

  @BeforeEach
  void registerEvent() {
    eventManager = new ReflectedEventManager();
    eventManager.registerEvents(new IgnoredCancelledEventTestListener());
  }

  @Test
  void callEvent_shouldIgnoreEventsAfterCancelled() {
    // Create CancellableEvent and call it.
    CancellableEvent event = new CancellableEvent("This should be changed.");
    eventManager.callEvent(event);

    // Assert results.
    assertTrue(event.isCancelled());
    assertEquals("CancelledIgnoreCancelled", event.getName());
  }

  /**
   * This event listener will be used to test that non 'ignoreCancelled' flagged event handlers were
   * skipped.
   */
  public static class IgnoredCancelledEventTestListener implements EventListener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void eventCanceller(CancellableEvent eve) {
      eve.setName("Cancelled");
      eve.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void noIgnoredEvent(CancellableEvent eve) {
      eve.setName(eve.getName() + "ThisShouldBeSkipped");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void ignoredEvent(CancellableEvent eve) {
      eve.setName(eve.getName() + "IgnoreCancelled");
    }
  }

  /** This event is used to prove event cancellation behaviors. */
  @Data
  @RequiredArgsConstructor
  public static class CancellableEvent extends Event implements EventCancellable {
    private boolean cancelled;
    @NonNull private String name;
  }
}
