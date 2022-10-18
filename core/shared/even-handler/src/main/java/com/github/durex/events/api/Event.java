package com.github.durex.events.api;

/**
 * Represents an event.
 *
 * @see EventCancellable
 */
@SuppressWarnings({"all"})
public abstract class Event {

  /**
   * Assert if the event can be cancelled.
   *
   * @return true if cancellable
   */
  public boolean isCancellable() {
    return (this instanceof EventCancellable);
  }

  /**
   * Check if the event is cancellable and it was cancelled.
   *
   * @return 'true' when was cancelled.
   */
  public boolean isCancelled() {
    return isCancellable() && ((EventCancellable) this).isCancelled();
  }
}
