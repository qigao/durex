package com.github.durex.music.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.sqlbuilder.enums.WildCardType;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PlaylistServiceWildcardContractTest {

  @ParameterizedTest
  @EnumSource(value = WildCardType.class, names = {"START_WITH", "END_WITH", "CONTAINS"})
  void findDelegatesRawTitleAndWildcardExactlyOnce(WildCardType wildcard) {
    var repository = mock(PlayListRepository.class);
    var relationRepository = mock(PlayListMusicRepository.class);
    when(repository.findByTitle("Spring", wildcard)).thenReturn(List.of());
    var service = new PlaylistService(repository, relationRepository);

    service.findPlayListByTitle("Spring", wildcard);

    verify(repository).findByTitle("Spring", wildcard);
  }

  @ParameterizedTest
  @EnumSource(value = WildCardType.class, names = {"START_WITH", "END_WITH", "CONTAINS"})
  void deleteDelegatesRawTitleAndWildcardExactlyOnce(WildCardType wildcard) {
    var repository = mock(PlayListRepository.class);
    var relationRepository = mock(PlayListMusicRepository.class);
    when(repository.deleteByTitle("Spring", wildcard)).thenReturn(1);
    var service = new PlaylistService(repository, relationRepository);

    service.deletePlayListByTitle("Spring", wildcard);

    verify(repository).deleteByTitle("Spring", wildcard);
  }
}
