package com.github.durex.music.api.client;

import com.github.durex.music.api.MusicApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.service.registry.ImportHttpServices;

@AutoConfiguration
@ImportHttpServices(group = "music", types = MusicApi.class)
public class MusicClientAutoConfiguration {}
