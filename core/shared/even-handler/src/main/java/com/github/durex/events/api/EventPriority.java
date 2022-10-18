package com.github.durex.events.api;

/**
 * Event execution order from MONITOR(First) to LOWEST(last). Order was calculated using the enum
 * ordinal.
 */
public enum EventPriority {
  MONITOR,
  HIGHEST,
  HIGH,
  NORMAL,
  LOW,
  LOWEST
}
