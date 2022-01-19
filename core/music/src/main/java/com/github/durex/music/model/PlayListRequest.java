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
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(name = "PlayListRequest", description = "歌单结构描述")
public class PlayListRequest {
  @Schema(description = "歌单ID,可以由服务端生成", example = "1231dfasdf234", required = true)
  @Size(max = 128, message = "歌单ID长度不能超过128")
  private String id;

  @Schema(description = "歌单名", example = "东风破西风", required = true)
  @NotBlank(message = "歌单名不能为空")
  @Size(max = 255, message = "歌单ID长度不能超过255")
  private String title;

  @Schema(description = "歌曲描述", example = "最厉害")
  @Size(max = 255, message = "歌曲描述长度不能超过255")
  private String description;

  @Schema(description = "图片链接", example = "http://music.163.com/music/cover/1316098.jpg")
  @Size(max = 1024, message = "图片链接长度不能超过1024")
  private String coverUrl;

  @Schema(description = "歌曲列表总时长,可为0，服务端会合计", example = "0")
  private int duration;

  @Schema(description = "设备ID", example = "12312bda")
  @Size(max = 255, message = "长度不能超过255")
  private String device;

  @Schema(description = "歌曲列表总数，可为0，服务端会合计", example = "0")
  private int total;

  @Schema(description = "歌曲列表")
  private List<MusicRequest> musics;
}
