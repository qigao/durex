package com.github.durex.music.mapper;

import com.github.durex.api.tables.records.RMusic;
import com.github.durex.music.api.Music;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MusicMapper {
  public void mapDtoToRecord(Music music, RMusic rMusic) {
    rMusic.setId(music.getId());
    rMusic.setTitle(music.getTitle());
    rMusic.setDescription(music.getDescription());

    rMusic.setArtistId(music.getArtistId());
    rMusic.setPlayId(music.getPlayId());
    rMusic.setLyricId(music.getLyricId());
    rMusic.setCoverId(music.getCoverId());

    rMusic.setSampleRate(String.valueOf(music.getSampleRate()));
    rMusic.setChannels(music.getChannels());
    rMusic.setBitRate(music.getBitRate());
    rMusic.setDuration(music.getDuration());

    rMusic.setMusicType(music.getMusicType());
  }

  public RMusic mapDtoToRecord(Music music) {
    return new RMusic()
        .setId(music.getId())
        .setTitle(music.getTitle())
        .setDescription(music.getDescription())
        .setArtistId(music.getArtistId())
        .setPlayId(music.getPlayId())
        .setLyricId(music.getLyricId())
        .setCoverId(music.getCoverId())
        .setSampleRate(String.valueOf(music.getSampleRate()))
        .setChannels(music.getChannels())
        .setBitRate(music.getBitRate())
        .setDuration(music.getDuration())
        .setMusicType(music.getMusicType());
  }

  public Music mapRecordToDto(RMusic rMusic) {
    return new Music()
        .withId(rMusic.getId())
        .withTitle(rMusic.getTitle())
        .withDescription(rMusic.getDescription())
        .withArtistId(rMusic.getArtistId())
        .withPlayId(rMusic.getPlayId())
        .withLyricId(rMusic.getLyricId())
        .withCoverId(rMusic.getCoverId())
        .withMusicType(rMusic.getMusicType())
        .withDuration(rMusic.getDuration())
        .withSampleRate(Integer.parseInt(rMusic.getSampleRate()))
        .withChannels(rMusic.getChannels())
        .withBitRate(rMusic.getBitRate());
  }
}
