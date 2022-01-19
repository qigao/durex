package com.github.durex.music.service;

import com.github.durex.music.exceptions.AlreadyExistsException;
import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.repository.PlayListMusicRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class PlayListMusicService {
  @Inject PlayListMusicRepository playListMusicRepository;

  public boolean checkMusicIdThenAddToPlayListWithOrder(
      String playListId, List<OrderedMusicRequest> musicIdWithOrder) {
    var musicIDs =
        musicIdWithOrder.stream().map(OrderedMusicRequest::getMusicId).collect(Collectors.toList());
    if (playListMusicRepository.checkMusicAlreadyExistsInPlayList(playListId, musicIDs) > 0) {
      throw new AlreadyExistsException("musicIds already in playList");
    }

    if (playListMusicRepository.checkIfMusicNotExists(musicIDs) != musicIdWithOrder.size()) {
      throw new NotFoundException("musicIds not exists");
    }
    List<String> availableMusicIds = playListMusicRepository.getMusicIdsByPlayListId(playListId);
    var musicCounts = availableMusicIds.size();
    musicIdWithOrder.forEach(
        m ->
            availableMusicIds.add(
                m.getOrder() > musicCounts ? musicCounts : m.getOrder(), m.getMusicId()));
    playListMusicRepository.emptyPlayList(playListId);

    return playListMusicRepository.addMusicToPlayListInBatch(playListId, availableMusicIds)
        == availableMusicIds.size();
  }

  public boolean checkMusicIdThenAddToPlayList(String playListId, List<String> musicIds) {
    if (playListMusicRepository.checkMusicAlreadyExistsInPlayList(playListId, musicIds) > 0) {
      throw new AlreadyExistsException("musicIds already in playList");
    }
    if (playListMusicRepository.checkIfMusicNotExists(musicIds) != musicIds.size()) {
      throw new NotFoundException("musicIds not exists");
    }
    return playListMusicRepository.addMusicToPlayListInBatch(playListId, musicIds)
        == musicIds.size();
  }

  public boolean checkMusicIdThenDeleteFromPlayList(String playListId, List<String> musicIds) {
    if (playListMusicRepository.checkMusicAlreadyExistsInPlayList(playListId, musicIds)
        != musicIds.size()) {
      throw new NotFoundException("musicIds not in playList");
    }
    return playListMusicRepository.deleteMusicFromPlayListInBatch(playListId, musicIds)
        == musicIds.size();
  }
}
