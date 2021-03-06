package com.github.durex.music.mapper;

import com.github.durex.model.tables.records.RPlaylist;
import com.github.durex.music.model.PlayList;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayListMapper {
  public void mapDtoToRecord(PlayList playList, RPlaylist rPlaylist) {
    rPlaylist.setId(playList.getId());
    rPlaylist.setDescription(playList.getDescription());
    rPlaylist.setTitle(playList.getTitle());
    rPlaylist.setCoverId(playList.getCoverId());
  }

  public RPlaylist mapDtoToRecord(PlayList playList) {
    return new RPlaylist()
        .setId(playList.getId())
        .setDescription(playList.getDescription())
        .setTitle(playList.getTitle())
        .setCoverId(playList.getCoverId());
  }

  public PlayList mapRecordToDto(RPlaylist rPlaylist) {
    return new PlayList()
        .withId(rPlaylist.getId())
        .withCoverId(rPlaylist.getCoverId())
        .withDescription(rPlaylist.getDescription())
        .withTitle(rPlaylist.getTitle());
  }
}
