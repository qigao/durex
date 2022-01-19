package com.github.durex.music.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@Cacheable
@Table(name = "music")
public class Music {
  @Id
  @Column(name = "id", unique = true, nullable = false, columnDefinition = "VARCHAR(128)")
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

  @Column(name = "device", columnDefinition = "VARCHAR(255)")
  private String device;

  @Column(name = "storyId", columnDefinition = "VARCHAR(255)")
  private String storyId;

  @Column(name = "description", columnDefinition = "VARCHAR(1024)")
  private String description;

  @Column(name = "create_time", columnDefinition = "DATETIME")
  private LocalDateTime createTime;

  @Column(name = "update_time", columnDefinition = "DATETIME")
  private LocalDateTime updateTime;

  @Column(name = "sampleRate", columnDefinition = "INT default 44")
  private int sampleRate;

  @Column(name = "channels", columnDefinition = "INT default 2")
  private int channels;

  @Column(name = "voiceName", columnDefinition = "VARCHAR(255)")
  private String voiceName;

  @OneToMany(mappedBy = "music")
  @ToString.Exclude
  private List<PlayListMusic> musicAssociations;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    Music music = (Music) o;
    return id != null && Objects.equals(id, music.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
