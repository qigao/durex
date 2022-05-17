package com.github.durex.music.mapper;

import com.github.durex.api.tables.records.RPlaylistMusic;
import com.github.durex.music.api.Music;
import com.github.durex.music.api.PlayListMusic;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayListMusicMapper {
  public List<RPlaylistMusic> mapDtoToRecord(PlayListMusic dto) {
    AtomicInteger order = new AtomicInteger(1);
    return dto.getMusics().stream()
        .map(
            music -> {
              RPlaylistMusic r = new RPlaylistMusic();
              r.setPlaylistId(dto.getId());
              r.setMusicId(music.getId());
              r.setMusicOrder(order.getAndIncrement());
              return r;
            })
        .collect(Collectors.toList());
  }

  public List<RPlaylistMusic> mapDtoToRecord(List<Music> dto, String playlistId) {
    AtomicInteger order = new AtomicInteger(1);
    return dto.stream()
        .map(
            music -> {
              var r = new RPlaylistMusic();
              r.setMusicId(music.getId());
              r.setMusicOrder(order.getAndIncrement());
              r.setPlaylistId(playlistId);
              return r;
            })
        .collect(Collectors.toList());
  }
}
