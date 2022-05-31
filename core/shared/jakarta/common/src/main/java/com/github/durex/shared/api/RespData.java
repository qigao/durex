package com.github.durex.shared.api;

import com.github.durex.shared.exceptions.model.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor(staticName = "of")
public class RespData<T> {
  private T result;
  private ErrorResponse error;
}
