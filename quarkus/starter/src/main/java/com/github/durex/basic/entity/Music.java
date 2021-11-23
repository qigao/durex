package com.github.durex.basic.entity;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@Cacheable
@EqualsAndHashCode(of = "id")
@Table(name = "music")
public class Music {
  @Id
  @Column(name = "id", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
  private String id;

  @Column(name = "playUrl", nullable = false, columnDefinition = "VARCHAR(1024)")
  private String playUrl;

  @Column(name = "duration", columnDefinition = "INT")
  private int duration;

  @Column(name = "coverUrl", columnDefinition = "VARCHAR(1024)")
  private String coverUrl;

  @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
  private String title;

  @Column(name = "musicType", nullable = false, columnDefinition = "INT default 0")
  private int musicType;

  @Column(name = "artist", columnDefinition = "VARCHAR(255)")
  private String artist;

  @Column(name = "editor", columnDefinition = "VARCHAR(255)")
  private String editor;

  @Column(name = "description", columnDefinition = "VARCHAR(1024)")
  private String description;

  @Column(name = "create_time", columnDefinition = "DATETIME")
  private LocalDateTime createTime;

  @Column(name = "update_time", columnDefinition = "DATETIME")
  private LocalDateTime updateTime;

  @ManyToMany(mappedBy = "musics")
  @ToString.Exclude
  private List<PlayList> playList;
}
