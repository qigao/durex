package com.github.durex.music.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.github.durex.music.exceptions.AlreadyExistsException;
import com.github.durex.music.model.OrderedMusicRequest;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.util.support.Data;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaylistMusicServiceMockedTest {
  @Inject PlayListMusicService playListMusicService;
  @InjectMock PlayListMusicRepository playListMusicRepository;
  public static final String EDITOR = "d1e5nqreqo";

  public static final String MUSIC_ID_1 = "wtVNDNP3YfqCOH7wKXStcEc61U1";
  public static final String MUSIC_ID_2 = "wtVNDNP3YfqCOH7wKXStcEc61U2";
  public static final String MUSIC_ID_3 = "wtVNDNP3YfqCOH7wKXStcEc61U3";
  public static final String MUSIC_ID_4 = "wtVNDNP3YfqCOH7wKXStcEc61U4";
  public static final String MUSIC_ID_5 = "wtVNDNP3YfqCOH7wKXStcEc61U5";
  public static final String MUSIC_ID_6 = "wtVNDNP3YfqCOH7wKXStcEc61U6";
  public static final String MUSIC_ID_7 = "wtVNDNP3YfqCOH7wKXStcEc61U7";
  public static final String MUSIC_ID_8 = "wtVNDNP3YfqCOH7wKXStcEc61U8";
  public static final String MUSIC_ID_9 = "wtVNDNP3YfqCOH7wKXStcEc61U9";
  public static final List<String> MUSIC_IDS =
      List.of(MUSIC_ID_1, MUSIC_ID_2, MUSIC_ID_3, MUSIC_ID_4);

  public static final List<String> MUSIC_IDS2 =
      List.of(MUSIC_ID_9, MUSIC_ID_6, MUSIC_ID_7, MUSIC_ID_8);
  private final String UNIQUE_ID = "wtVNDNP3YfqCOH7wKXStcEc61U1";

  @Test
  void testCheckMusicIdThenAddToPlayListWithOrderWithAlreadyExistsException() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(1L);
    List<OrderedMusicRequest> musicIdWithOrder = Data.givenMusicsWithOrder();
    assertThrows(
        AlreadyExistsException.class,
        () ->
            playListMusicService.checkMusicIdThenAddToPlayListWithOrder(
                UNIQUE_ID, musicIdWithOrder));
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWithOrderWithNotExistsException() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(any())).thenReturn(1L);
    var musicsWithOrder = Data.givenMusicsWithOrder();
    assertThrows(
        NotFoundException.class,
        () ->
            playListMusicService.checkMusicIdThenAddToPlayListWithOrder(
                UNIQUE_ID, musicsWithOrder));
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWithOrderReturnTrue() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(any())).thenReturn(4L);
    when(playListMusicRepository.getMusicIdsByPlayListId(any()))
        .thenReturn(new ArrayList<>(MUSIC_IDS));
    doNothing().when(playListMusicRepository).emptyPlayList(any());
    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);

    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any()))
        .thenReturn(MUSIC_IDS.size() + MUSIC_IDS2.size());
    List<OrderedMusicRequest> musicIdWithOrder = Data.givenMusicsWithOrder();
    var result =
        playListMusicService.checkMusicIdThenAddToPlayListWithOrder(UNIQUE_ID, musicIdWithOrder);
    assertTrue(result);
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWithOrderReturnFalse() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(any())).thenReturn(4L);
    when(playListMusicRepository.getMusicIdsByPlayListId(any()))
        .thenReturn(new ArrayList<>(MUSIC_IDS));
    doNothing().when(playListMusicRepository).emptyPlayList(any());
    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);

    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);
    List<OrderedMusicRequest> musicIdWithOrder = Data.givenMusicsWithOrder();
    var result =
        playListMusicService.checkMusicIdThenAddToPlayListWithOrder(UNIQUE_ID, musicIdWithOrder);
    assertFalse(result);
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWithOrderAddToEmptyPlayList() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(any())).thenReturn(4L);
    when(playListMusicRepository.getMusicIdsByPlayListId(any())).thenReturn(new ArrayList<>());
    doNothing().when(playListMusicRepository).emptyPlayList(any());
    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);

    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);
    List<OrderedMusicRequest> musicIdWithOrder = Data.givenMusicsWithOrder();
    var result =
        playListMusicService.checkMusicIdThenAddToPlayListWithOrder(UNIQUE_ID, musicIdWithOrder);
    assertTrue(result);
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWhenMusicAlreadyExists() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(1L);
    assertThrows(
        AlreadyExistsException.class,
        () -> playListMusicService.checkMusicIdThenAddToPlayList(UNIQUE_ID, MUSIC_IDS));
  }

  @Test
  void testCheckMusicIdThenAddToPlayListWhenMusicDoesNotExists() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    assertThrows(
        NotFoundException.class,
        () -> playListMusicService.checkMusicIdThenAddToPlayList(UNIQUE_ID, MUSIC_IDS));
  }

  @Test
  void testCheckMusicIdThenAddToPlayListThenAddToPlaylist() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(MUSIC_IDS)).thenReturn(4L);
    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(4);
    var result = playListMusicService.checkMusicIdThenAddToPlayList(UNIQUE_ID, MUSIC_IDS);
    assertTrue(result);
  }

  @Test
  void testCheckMusicIdThenAddToPlayListThenAddToPlaylistWithOneFailed() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    when(playListMusicRepository.checkIfMusicNotExists(MUSIC_IDS)).thenReturn(4L);
    when(playListMusicRepository.addMusicToPlayListInBatch(any(), any())).thenReturn(3);
    var result = playListMusicService.checkMusicIdThenAddToPlayList(UNIQUE_ID, MUSIC_IDS);
    assertFalse(result);
  }

  @Test
  void testCheckMusicIdThenDeleteFromPlayListWithException() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(0L);
    assertThrows(
        NotFoundException.class,
        () -> playListMusicService.checkMusicIdThenDeleteFromPlayList(UNIQUE_ID, MUSIC_IDS));
  }

  @Test
  void testCheckMusicIdThenDeleteFromPlayListReturnSuccess() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(4L);
    when(playListMusicRepository.deleteMusicFromPlayListInBatch(any(), any())).thenReturn(4);
    var result = playListMusicService.checkMusicIdThenDeleteFromPlayList(UNIQUE_ID, MUSIC_IDS);
    assertTrue(result);
  }

  @Test
  void testCheckMusicIdThenDeleteFromPlayListReturnOneFailed() {
    when(playListMusicRepository.checkMusicAlreadyExistsInPlayList(any(), any())).thenReturn(4L);
    when(playListMusicRepository.deleteMusicFromPlayListInBatch(any(), any())).thenReturn(3);
    var result = playListMusicService.checkMusicIdThenDeleteFromPlayList(UNIQUE_ID, MUSIC_IDS);
    assertFalse(result);
  }
}
