package com.github.durex.music.util;

import com.github.durex.music.entity.Music;
import com.github.durex.music.entity.PlayList;
import com.github.durex.music.entity.PlayListMusic;
import com.github.durex.music.model.MusicRequest;
import com.github.durex.music.model.MusicResp;
import com.github.durex.music.model.PlayListRequest;
import com.github.durex.music.model.PlayListResp;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityMapper {
  public static MusicResp musicEntityToResp(Music music, int order) {
    return MusicResp.builder()
        .artist(music.getArtist())
        .coverUrl(music.getCoverUrl())
        .description(music.getDescription())
        .playUrl(music.getPlayUrl())
        .id(music.getId())
        .duration(music.getDuration())
        .title(music.getTitle())
        .channels(music.getChannels())
        .sampleRate(music.getSampleRate())
        .musicType(music.getMusicType())
        .order(order)
        .voiceName(music.getVoiceName())
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
        .channels(musicRequest.getChannels())
        .sampleRate(musicRequest.getSampleRate())
        .musicType(musicRequest.getMusicType())
        .device(musicRequest.getDevice())
        .storyId(musicRequest.getStoryId())
        .voiceName(musicRequest.getVoiceName())
        .build();
  }

  public static Music musicJPARequestToEntity(MusicRequest musicRequest, String editor) {
    return Music.builder()
        .artist(musicRequest.getArtist())
        .coverUrl(musicRequest.getCoverUrl())
        .description(musicRequest.getDescription())
        .playUrl(musicRequest.getPlayUrl())
        .id(musicRequest.getId())
        .duration(musicRequest.getDuration())
        .title(musicRequest.getTitle())
        .channels(musicRequest.getChannels())
        .sampleRate(musicRequest.getSampleRate())
        .musicType(musicRequest.getMusicType())
        .device(musicRequest.getDevice())
        .storyId(musicRequest.getStoryId())
        .editor(editor)
        .voiceName(musicRequest.getVoiceName())
        .build();
  }

  public static PlayListResp playListToResp(PlayList playList) {
    var musicsCount =
        playList.getPlayListAssociations().stream().map(PlayListMusic::getMusic).count();
    var musicsDuration =
        playList.getPlayListAssociations().stream()
            .map(PlayListMusic::getMusic)
            .mapToLong(Music::getDuration)
            .sum();
    var musicsList =
        playList.getPlayListAssociations().stream()
            .map(PlayListMusic::getMusic)
            .collect(Collectors.toList());
    return PlayListResp.builder()
        .id(playList.getId())
        .title(playList.getTitle())
        .description(playList.getDescription())
        .coverUrl(playList.getCoverUrl())
        .description(playList.getDescription())
        .duration(Math.toIntExact(musicsDuration))
        .total(Math.toIntExact(musicsCount))
        .musics(musicListToMusicRespMapper(musicsList))
        .build();
  }

  public static PlayList playListRequestToJPAEntity(
      PlayListRequest playListRequest, String editor) {
    return PlayList.builder()
        .id(playListRequest.getId())
        .title(playListRequest.getTitle())
        .description(playListRequest.getDescription())
        .coverUrl(playListRequest.getCoverUrl())
        .editor(editor)
        .device(playListRequest.getDevice())
        .build();
  }

  public static List<PlayListResp> playListToResp(List<PlayList> playList) {
    return playList.stream().map(EntityMapper::playListToResp).collect(Collectors.toList());
  }

  public static List<MusicResp> musicListToMusicRespMapper(List<Music> musicList) {
    AtomicInteger order = new AtomicInteger(1);
    return musicList.stream()
        .map(music -> musicEntityToResp(music, order.getAndIncrement()))
        .collect(Collectors.toList());
  }
}
