package com.github.durex.basic.model;

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
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(name = "PlayListRequest", description = "PlayList of music files")
public class PlayListRequest {
  @Schema(description = "歌单ID", required = true)
  @NotBlank
  private Long id;

  @Schema(description = "歌单名", example = "东风破西风", required = true)
  @NotBlank(message = "歌单名不能为空")
  @Size(max = 250, message = "歌单名长度不能超过250")
  private String title;

  @Schema(description = "描述", example = "最厉害")
  @Size(max = 100, message = "描述长度不能超过100")
  private String description;

  @Schema(
      description = "图片链接",
      example = "http://music.mummyway.com/music/cover/1316098.jpg",
      required = true)
  @NotBlank(message = "图片链接不能为空")
  @Size(max = 250, message = "播放地址长度不能超过 250")
  private String cover;

  @Schema(description = "歌曲列表总时长", example = "2000")
  private int duration;

  @Schema(description = "歌曲列表总数", example = "20")
  private int total;

  @Schema(description = "歌曲列表")
  private List<MusicRequest> musics;
}
