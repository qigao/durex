package com.github.durex.music.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "RespData", description = "返回结构体")
public class RespData<T> {
  @Schema(description = "HTTP返回结果码", example = "200")
  private int result;

  @Schema(description = "结果码为200时,范围success,其余返回相应的错误原因", example = "success")
  private String msg;

  @Schema(description = "结果码为200时返回数据，其余为空", example = "相应的数据结构")
  private T data;
}
