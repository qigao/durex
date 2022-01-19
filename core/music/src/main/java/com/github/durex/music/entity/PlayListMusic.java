package com.github.durex.music.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "playlist_music")
@IdClass(PlayListMusicId.class)
@ToString
@Setter
@Getter
public class PlayListMusic {
  @Id
  @ManyToOne
  @JoinColumn(name = "playlistId", referencedColumnName = "id")
  private PlayList playList;

  @Id
  @ManyToOne
  @JoinColumn(name = "musicId", referencedColumnName = "id")
  private Music music;

  @Column(name = "musicOrder")
  private int musicOrder;
}
