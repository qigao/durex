package com.github.durex.events;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventCancellable;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.internal.ReflectedEventManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This class tests that event cancellation works as expected. */
class EventManagerCancellableEventsIntegrationTest {

  private EventManager eventManager;

  @BeforeEach
  void registerEventManager() {
    eventManager = new ReflectedEventManager();
    eventManager.registerEvents(new EventCancellationListener());
  }

  @Test
  void canceledEvent_shouldBeCancelled_whenEventListenerCancelsIt() {
    CancellableEvent event = new CancellableEvent(true);
    eventManager.callEvent(event);
    assertTrue(event.isCancelled());
  }

  @Test
  void nonCanceledEvent_shouldBeNotCancelled_whenEventListenerDoesNotCancelsIt() {
    CancellableEvent event = new CancellableEvent(false);
    eventManager.callEvent(event);
    assertFalse(event.isCancelled());
  }

  /** This class will cancel events flagged to be canceled. */
  public static class EventCancellationListener implements EventListener {
    @EventHandler
    public void cancellableEvent(CancellableEvent event) {
      event.setCancelled(event.isToBeCancelled());
    }
  }

  /** This event will be used to test event cancellation. */
  @Getter
  @Setter
  @RequiredArgsConstructor
  public static class CancellableEvent extends Event implements EventCancellable {
    private final boolean toBeCancelled;
    private boolean cancelled;
  }
}
