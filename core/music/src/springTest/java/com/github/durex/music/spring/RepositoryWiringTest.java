package com.github.durex.music.spring;

import static com.github.durex.model.tables.QMusic.MUSIC;
import static com.github.durex.model.tables.QPlaylist.PLAYLIST;
import static com.github.durex.sqlbuilder.enums.WildCardType.CONTAINS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.repository.CreatorPlayListRepository;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = MusicSpringApplication.class)
class RepositoryWiringTest {
  @Autowired MusicRepository musicRepository;
  @Autowired PlayListRepository playListRepository;
  @Autowired PlayListMusicRepository playListMusicRepository;
  @Autowired CreatorPlayListRepository creatorPlayListRepository;
  @Autowired DSLContext dsl;

  @Test
  void repositoriesAreConstructedBySpringWithoutCdi() {
    assertNotNull(musicRepository);
    assertNotNull(playListRepository);
    assertNotNull(playListMusicRepository);
    assertNotNull(creatorPlayListRepository);
  }

  @Test
  void findAllExcludesSoftDeletedRows() {
    assertEquals(
        List.of("music-1"), musicRepository.findAll().stream().map(Music::getId).toList());
    assertEquals(
        List.of("playlist-1"),
        playListRepository.findAll().stream().map(PlayList::getId).toList());
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void normalDeletesIgnoreAlreadyDeletedRowsWithoutRefreshingDeleteTime() {
    var musicDeleteTimeBefore =
        dsl.select(MUSIC.DELETE_TIME)
            .from(MUSIC)
            .where(MUSIC.ID.eq("music-deleted"))
            .fetchOne(MUSIC.DELETE_TIME);
    var playlistDeleteTimeBefore =
        dsl.select(PLAYLIST.DELETE_TIME)
            .from(PLAYLIST)
            .where(PLAYLIST.ID.eq("playlist-deleted"))
            .fetchOne(PLAYLIST.DELETE_TIME);

    assertAll(
        () -> assertEquals(0, musicRepository.deleteById("music-deleted")),
        () -> assertEquals(0, musicRepository.deleteByTitle("Deleted Song")),
        () -> assertEquals(0, musicRepository.deleteByTitle("Deleted", CONTAINS)),
        () -> assertEquals(0, musicRepository.delete(List.of("music-deleted"))),
        () -> assertEquals(0, playListRepository.deleteById("playlist-deleted")),
        () -> assertEquals(0, playListRepository.deleteById(List.of("playlist-deleted"))),
        () -> assertEquals(0, playListRepository.deleteByTitle("Deleted Playlist")),
        () -> assertEquals(0, playListRepository.deleteByTitle("Deleted", CONTAINS)));

    assertEquals(
        musicDeleteTimeBefore,
        dsl.select(MUSIC.DELETE_TIME)
            .from(MUSIC)
            .where(MUSIC.ID.eq("music-deleted"))
            .fetchOne(MUSIC.DELETE_TIME));
    assertEquals(
        playlistDeleteTimeBefore,
        dsl.select(PLAYLIST.DELETE_TIME)
            .from(PLAYLIST)
            .where(PLAYLIST.ID.eq("playlist-deleted"))
            .fetchOne(PLAYLIST.DELETE_TIME));
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void normalUpdatesIgnoreAlreadyDeletedRows() {
    String musicTitleBefore =
        dsl.select(MUSIC.TITLE)
            .from(MUSIC)
            .where(MUSIC.ID.eq("music-deleted"))
            .fetchOne(MUSIC.TITLE);
    String playlistTitleBefore =
        dsl.select(PLAYLIST.TITLE)
            .from(PLAYLIST)
            .where(PLAYLIST.ID.eq("playlist-deleted"))
            .fetchOne(PLAYLIST.TITLE);

    var music = deletedMusic("mutated deleted music");
    var playlist = deletedPlaylist("mutated deleted playlist");
    var batchMusic = deletedMusic("batch mutated deleted music");
    var batchPlaylist = deletedPlaylist("batch mutated deleted playlist");

    assertAll(
        () -> assertEquals(0, musicRepository.update(music)),
        () -> assertEquals(0, playListRepository.update(playlist)),
        () -> assertArrayEquals(new int[] {0}, musicRepository.update(List.of(batchMusic))),
        () -> assertArrayEquals(new int[] {0}, playListRepository.update(List.of(batchPlaylist))));

    assertEquals(
        musicTitleBefore,
        dsl.select(MUSIC.TITLE)
            .from(MUSIC)
            .where(MUSIC.ID.eq("music-deleted"))
            .fetchOne(MUSIC.TITLE));
    assertEquals(
        playlistTitleBefore,
        dsl.select(PLAYLIST.TITLE)
            .from(PLAYLIST)
            .where(PLAYLIST.ID.eq("playlist-deleted"))
            .fetchOne(PLAYLIST.TITLE));
  }

  private static Music deletedMusic(String title) {
    var music = new Music();
    music.setId("music-deleted");
    music.setTitle(title);
    music.setDescription("soft-delete-fixture");
    music.setDuration(120);
    music.setSampleRate(44100);
    music.setBitRate(192);
    music.setChannels(2);
    music.setMusicType(0);
    music.setArtistId("artist-2");
    music.setCoverId("cover-2");
    music.setPlayId("play-2");
    music.setLyricId("lyric-2");
    return music;
  }

  private static PlayList deletedPlaylist(String title) {
    var playlist = new PlayList();
    playlist.setId("playlist-deleted");
    playlist.setTitle(title);
    playlist.setDescription("soft-delete-fixture");
    playlist.setCoverId("cover-2");
    return playlist;
  }
}
