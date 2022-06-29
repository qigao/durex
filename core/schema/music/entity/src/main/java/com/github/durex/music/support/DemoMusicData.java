package com.github.durex.music.support;

import com.github.durex.music.model.Music;
import com.github.durex.music.model.PlayList;
import com.github.durex.music.model.PlayListMusic;
import com.github.durex.uniqid.UniqID;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

@UtilityClass
public class DemoMusicData {

  public Music givenAMusic(String musicId) {
    var title = RandomStringUtils.randomAlphabetic(10);
    var description = RandomStringUtils.randomAlphabetic(100);
    return givenMusicByTitle(musicId, title, description);
  }

  public List<Music> givenSomeMusics(int num) {
    var musicLists = new ArrayList<Music>();
    for (int i = 0; i < num; i++) {
      musicLists.add(givenAMusic());
    }
    return musicLists;
  }

  public Music givenAMusic() {
    var musicId = UniqID.getId();
    return givenAMusic(musicId);
  }

  public Music givenMusicByTitle(String musicId, String title, String description) {
    return new Music()
        .withId(musicId)
        .withTitle(title)
        .withDescription(description)
        .withPlayId(musicId)
        .withCoverId(musicId)
        .withArtistId(musicId)
        .withLyricId(musicId)
        .withDuration(100)
        .withSampleRate(44100)
        .withChannels(2)
        .withBitRate(128)
        .withMusicType(1);
  }

  public List<PlayList> givenSomePlayList(int num) {
    var playLists = new ArrayList<PlayList>();
    for (int i = 0; i < num; i++) {
      playLists.add(givenAPlayList());
    }
    return playLists;
  }

  public PlayList givenAPlayList() {
    var playListId = UniqID.getId();
    return givenAPlayList(playListId);
  }

  public PlayList givenAPlayList(String playListId) {

    var title = RandomStringUtils.randomAlphabetic(50);
    return givenPlayListByTitle(playListId, title);
  }

  public PlayList givenPlayListByTitle(String playListId, String title) {
    return givenAPlayList(playListId, title, RandomStringUtils.randomAlphanumeric(100));
  }

  public PlayList givenAPlayList(String playListId, String title, String description) {
    return new PlayList().withId(playListId).withTitle(title).withDescription(description);
  }

  public PlayListMusic givenAPlayListMusic() {
    var musics = givenSomeMusics(5);
    var title = RandomStringUtils.randomAlphabetic(50);
    var description = RandomStringUtils.randomAlphabetic(100);
    return new PlayListMusic()
        .withId(UniqID.getId())
        .withCoverId(UniqID.getId())
        .withTitle(title)
        .withDescription(description)
        .withMusics(musics);
  }
}
