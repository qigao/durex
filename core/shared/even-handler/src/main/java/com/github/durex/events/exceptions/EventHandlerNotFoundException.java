package com.github.durex.events.exceptions;

/**
 * This exception will be throw when something went wrong with an event handler.
 *
 */
public class EventHandlerNotFoundException extends RuntimeException {

  private static final String PREFIX = "Unable to find Event > ";

  public EventHandlerNotFoundException(String message) {
    super(PREFIX + message);
  }

  public EventHandlerNotFoundException(String message, Throwable throwable) {
    super(PREFIX + message, throwable);
  }
}
