package com.github.durex.events.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.durex.events.EventManager;
import com.github.durex.events.api.Event;
import com.github.durex.events.api.EventHandler;
import com.github.durex.events.api.EventListener;
import com.github.durex.events.api.EventPriority;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test will prove that EventManagement are sorting event handlers by his priority as excepted.
 */
class EventManagementEventPrioritySortingTest {

  private EventManager eventManager;
  private EventManagement eventManagement;

  @BeforeEach
  void registerEventManager() throws NoSuchFieldException, IllegalAccessException {
    eventManager = new ReflectedEventManager();
    eventManagement = getEventManagement();
  }

  @Test
  void
      getEventHandlersFor_shouldReturnEventsSortedByPriority_whenProvidedListenerHasDifferentPrioritizedEvents()
          throws NoSuchFieldException, IllegalAccessException {
    // Register listeners with prioritized events
    eventManager.registerEvents(new PriorityTestListener());

    // Get events from Listeners
    List<RegisteredEventHandler> list = eventManagement.getEventHandlerFor(TestEvent.class);

    // Assert order
    assertEquals(
        "com.github.durex.events.remote.EventManagementEventPrioritySortingTest.PriorityTestListener#monitorEvent",
        list.get(0).getName());

    // Substring name cause doesn't matters if #nonTagEvent, #nonTagEventTwo or #nonTagEventTree are
    // fired in different order.
    String expectedCanonicalName =
        "com.github.durex.events.remote.EventManagementEventPrioritySortingTest.PriorityTestListener#nonTagEvent";

    final int expectedLength = expectedCanonicalName.length();
    assertEquals(expectedCanonicalName, list.get(1).getName().substring(0, expectedLength));
    assertEquals(expectedCanonicalName, list.get(2).getName().substring(0, expectedLength));
    assertEquals(expectedCanonicalName, list.get(3).getName().substring(0, expectedLength));

    assertEquals(
        "com.github.durex.events.remote.EventManagementEventPrioritySortingTest.PriorityTestListener#lowEvent",
        list.get(4).getName());
    assertEquals(
        "com.github.durex.events.remote.EventManagementEventPrioritySortingTest.PriorityTestListener#lowestEvent",
        list.get(5).getName());
  }

  private EventManagement getEventManagement() throws NoSuchFieldException, IllegalAccessException {
    Field field = eventManager.getClass().getDeclaredField("eventManagement");
    field.setAccessible(true);
    return (EventManagement) field.get(eventManager);
  }

  /** This event listener will be used to test the sorting of different events by his priority. */
  public static class PriorityTestListener implements EventListener {
    @EventHandler
    public void nonTagEvent(TestEvent eve) {}

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorEvent(TestEvent eve) {}

    @EventHandler
    public void nonTagEventTwo(TestEvent eve) {}

    @EventHandler(priority = EventPriority.LOW)
    public void lowEvent(TestEvent eve) {}

    @EventHandler(priority = EventPriority.LOWEST)
    public void lowestEvent(TestEvent eve) {}

    @EventHandler
    public void nonTagEventTree(TestEvent eve) {}
  }

  /** This testing event is used to create different prioritized event handlers. */
  public static class TestEvent extends Event {}
}
