package com.github.durex.shared.exceptions.model;

public enum ErrorCode {
  ENTITY_NOT_FOUND("entity.not.found"),
  UPDATE_ERROR("update.error"),
  INSERT_ERROR("insert.error"),
  DELETE_ERROR("delete.error"),
  NOTHING_FAILED("nothing.failed"),
  UNKNOWN_ERROR("unknown.error"),
  EMPTY_PARAM("empty.param"),
  ;

  private String code;

  ErrorCode(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
