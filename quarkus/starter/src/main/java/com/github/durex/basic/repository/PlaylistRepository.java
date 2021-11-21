package com.github.durex.basic.repository;

import com.github.durex.basic.entity.PlayList;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlaylistRepository implements PanacheRepository<PlayList> {}
