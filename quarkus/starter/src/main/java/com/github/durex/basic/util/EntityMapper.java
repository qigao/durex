package com.github.durex.basic.util;

import com.github.durex.basic.entity.Music;
import com.github.durex.basic.entity.PlayList;
import com.github.durex.basic.model.MusicRequest;
import com.github.durex.basic.model.PlayListRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityMapper {
  public static MusicRequest musicEntityToRequest(Music music) {
    return MusicRequest.builder()
        .artist(music.getArtist())
        .coverUrl(music.getCoverUrl())
        .description(music.getDescription())
        .playUrl(music.getPlayUrl())
        .id(music.getId())
        .duration(music.getDuration())
        .title(music.getTitle())
        .musicType(music.getMusicType())
        .build();
  }

  public static Music musicRequestToEntity(MusicRequest musicRequest) {
    return Music.builder()
        .artist(musicRequest.getArtist())
        .coverUrl(musicRequest.getCoverUrl())
        .description(musicRequest.getDescription())
        .playUrl(musicRequest.getPlayUrl())
        .id(musicRequest.getId())
        .duration(musicRequest.getDuration())
        .title(musicRequest.getTitle())
        .musicType(musicRequest.getMusicType())
        .build();
  }

  public static PlayListRequest playListToRequest(PlayList playList) {
    List<MusicRequest> m3u8EntityList = musicRequestListMapper(playList);
    return PlayListRequest.builder()
        .id(playList.getId())
        .title(playList.getTitle())
        .description(playList.getDescription())
        .coverUrl(playList.getCoverUrl())
        .total(m3u8EntityList.size())
        .description(playList.getDescription())
        .duration(playList.getDuration())
        .musics(m3u8EntityList)
        .build();
  }

  public static List<PlayListRequest> playListToRequest(List<PlayList> playList) {
    return playList.stream().map(EntityMapper::playListToRequest).collect(Collectors.toList());
  }

  public static List<MusicRequest> musicRequestListMapper(PlayList playList) {
    var musicList = playList.getMusics();
    return musicList.stream().map(EntityMapper::musicEntityToRequest).collect(Collectors.toList());
  }

  public static List<Music> musicEntityListMapper(PlayListRequest playList) {
    var m3U8TOList = playList.getMusics();
    return m3U8TOList.stream().map(EntityMapper::musicRequestToEntity).collect(Collectors.toList());
  }

  public static List<MusicRequest> musicRequestListMapper(List<Music> musicList) {
    return musicList.stream().map(EntityMapper::musicEntityToRequest).collect(Collectors.toList());
  }
}
