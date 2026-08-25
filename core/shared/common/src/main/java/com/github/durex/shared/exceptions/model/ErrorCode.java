package com.github.durex.shared.exceptions.model;

/** Stable structured error codes used by the Durex HTTP error model. */
public enum ErrorCode {
  /** The requested entity could not be found. */
  ENTITY_NOT_FOUND("entity.not.found"),

  /** An update operation failed. */
  UPDATE_ERROR("update.error"),

  /** A save operation failed. */
  SAVE_ERROR("save.error"),

  /** A delete operation failed. */
  DELETE_ERROR("delete.error"),

  /** Sentinel indicating that no failure occurred. */
  NOTHING_FAILED("nothing.failed"),

  /** An error occurred that has no more specific classification. */
  UNKNOWN_ERROR("unknown.error"),

  /** A required parameter was empty or missing. */
  EMPTY_PARAM("empty.param"),

  /** A supplied value failed validation. */
  VALUE_ERROR("value.error"),

  /** A general application operation failed. */
  OPERATION_FAILED("operation.failed");

  /** Stable serialized code value used in error responses. */
  private final String code;

  ErrorCode(String code) {
    this.code = code;
  }

  /**
   * Returns the stable serialized code value.
   *
   * @return dot-separated error code
   */
  public String getCode() {
    return code;
  }
}
