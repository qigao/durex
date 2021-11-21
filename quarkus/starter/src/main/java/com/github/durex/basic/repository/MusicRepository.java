package com.github.durex.basic.repository;

import com.github.durex.basic.entity.Music;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MusicRepository implements PanacheRepository<Music> {}
