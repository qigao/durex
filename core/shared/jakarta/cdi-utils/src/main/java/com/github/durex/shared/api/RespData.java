package com.github.durex.shared.api;

import com.github.durex.shared.exceptions.model.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespData {
  private Object data;
  private ErrorResponse error;
}
