package com.github.durex.music.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
@Table(name = "playlist")
public class PlayList {
  @Id
  @Column(name = "id", unique = true, nullable = false, columnDefinition = "VARCHAR(128)")
  private String id;

  @Column(name = "title", columnDefinition = "VARCHAR(255)")
  private String title;

  @Column(name = "description", columnDefinition = "VARCHAR(255)")
  private String description;

  @Column(name = "coverUrl", columnDefinition = "VARCHAR(1024)")
  private String coverUrl;

  @Column(name = "editor", columnDefinition = "VARCHAR(255)")
  private String editor;

  @Column(name = "device", columnDefinition = "VARCHAR(255)")
  private String device;

  @Column(name = "create_time", columnDefinition = "DATETIME")
  private LocalDateTime createTime;

  @Column(name = "update_time", columnDefinition = "DATETIME")
  private LocalDateTime updateTime;

  @OneToMany(mappedBy = "playList")
  @ToString.Exclude
  @OrderBy("musicOrder")
  private List<PlayListMusic> playListAssociations;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    PlayList that = (PlayList) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
