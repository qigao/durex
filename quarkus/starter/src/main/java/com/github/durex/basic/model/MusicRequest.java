package com.github.durex.basic.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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
@Schema(name = "MusicRequest", description = "Music Request Schema")
public class MusicRequest {
  @Schema(description = "歌曲ID")
  private @NotBlank String id;

  @Schema(description = "曲名", example = "东风破", required = true)
  @NotBlank(message = "曲名不能为空")
  private String title;

  @Schema(description = "演唱者", example = "周小文", required = true)
  @NotBlank(message = "演唱者不能为空")
  private String artist;

  @Schema(description = "描述", example = "最厉害")
  private String description;

  @Schema(description = "声音类型", example = "妈妈音", required = true)
  @NotBlank(message = "声音类型不能为空,由手机端定义")
  private String voice;

  @Schema(
      description = "播放链接",
      example = "http://music.mummyway.com/music/media/1316098.m3u8",
      required = true)
  @NotBlank(message = "播放链接不能为空")
  private String url;

  @Min(value = 1, message = "最小值为 1")
  @Max(value = 3000, message = "不能超过 3000")
  private int duration;

  @Schema(
      description = "图片链接",
      example = "http://music.mummyway.com/music/cover/1316098.jpg",
      required = true)
  @NotBlank(message = "图片链接不能为空")
  private String cover;
}
