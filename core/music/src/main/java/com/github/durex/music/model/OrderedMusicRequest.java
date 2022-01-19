package com.github.durex.music.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(name = "OrderedMusicRequest", description = "歌曲ID及其顺序,不能为空")
public class OrderedMusicRequest {
  @Schema(description = "歌曲ID列表")
  @NotBlank(message = "歌曲ID列表不能为空")
  @Size(min = 1, max = 1000, message = "歌曲ID列表长度不能超过1000")
  private String musicId;

  @Schema(description = "列表中的顺序")
  @NotBlank(message = "列表中的顺序不能为空")
  private Integer order;
}
