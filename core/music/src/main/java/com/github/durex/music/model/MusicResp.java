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
@Schema(name = "MusicResp", description = "歌曲结构描述，注：字符串均未限制长度")
public class MusicResp {
  @Schema(description = "歌曲ID", required = true, example = "1231dfasdf234")
  @NotBlank(message = "歌曲ID不能为空")
  @Size(max = 128, message = "歌曲ID长度不能超过128")
  private String id;

  @Schema(description = "曲名", example = "东风破", required = true)
  @NotBlank(message = "曲名不能为空")
  @Size(max = 255, message = "曲名长度不能超过255")
  private String title;

  @Schema(description = "演唱者", example = "周小文", required = true)
  @NotBlank(message = "演唱者不能为空")
  @Size(max = 255, message = "演唱者长度不能超过255")
  private String artist;

  @Schema(description = "音乐描述", example = "最厉害")
  @Size(max = 255, message = "音乐描述长度不能超过255")
  private String description;

  @Schema(description = "音乐来源,0：小象，1：喜马拉雅,2:问问", example = "0")
  private int musicType;

  @Schema(description = "采样率，单位：K", example = "44")
  private int sampleRate;

  @Schema(description = "声道数，单位：个", example = "2")
  private int channels;

  @Schema(description = "音乐文件在列表中的顺序", example = "1")
  private int order;

  @Schema(description = "voice model Name", example = "mum's voice")
  @Size(max = 255, message = "长度不能超过255")
  private String voiceName;

  @Schema(
      description = "播放链接",
      example = "http://music.163.com/music/media/1316098.m3u8",
      required = true)
  @NotBlank(message = "播放链接不能为空")
  @Size(max = 1024, message = "播放链接长度不能超过1024")
  private String playUrl;

  @Schema(description = "播放时长", example = "10", required = true)
  private int duration;

  @Schema(
      description = "图片链接",
      example = "http://music.163.com/music/cover/1316098.jpg",
      required = true)
  @NotBlank(message = "图片链接不能为空")
  @Size(max = 1024, message = "图片链接长度不能超过1024")
  private String coverUrl;
}