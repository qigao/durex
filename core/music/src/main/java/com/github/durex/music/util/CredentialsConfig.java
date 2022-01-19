package com.github.durex.music.util;

import io.smallrye.config.*;

@ConfigMapping(prefix = "credentials")
public interface CredentialsConfig {

  @WithName("appKey")
  @WithDefault("1234")
  String getAppKey();

  @WithName("token")
  @WithDefault("1234abcd")
  String getToken();
}
