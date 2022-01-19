package com.github.durex.music.model;

import java.util.List;
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
@Schema(name = "MusicIDListRequest", description = "歌曲结构描述，注：字符串均未限制长度")
public class MusicIDListRequest {
  @Schema(description = "歌曲ID列表")
  @NotBlank(message = "歌曲ID列表不能为空")
  @Size(min = 1, max = 1000, message = "歌曲ID列表长度不能超过1000")
  private List<String> musics;
}
