package com.github.durex.music.mapper;

import com.github.durex.api.tables.records.RPlaylist;
import com.github.durex.music.api.PlayList;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayListMapper {
  public void mapDtoToRecord(PlayList playList, RPlaylist rPlaylist) {
    rPlaylist.setId(playList.getId());
    rPlaylist.setDescription(playList.getDescription());
    rPlaylist.setTitle(playList.getTitle());
    rPlaylist.setCoverId(playList.getCoverId());
  }

  public PlayList mapRecordToDto(RPlaylist rPlaylist) {
    return new PlayList()
        .withId(rPlaylist.getId())
        .withCoverId(rPlaylist.getCoverId())
        .withDescription(rPlaylist.getDescription())
        .withTitle(rPlaylist.getTitle());
  }
}
