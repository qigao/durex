package com.github.durex.music.util.support;

import com.github.durex.music.util.CredentialsConfig;
import io.smallrye.config.SmallRyeConfig;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class CredentialsConfigMock {
  @Inject SmallRyeConfig config;

  @Produces
  @ApplicationScoped
  @io.quarkus.test.Mock
  CredentialsConfig credentialsConfig() {
    return config.unwrap(SmallRyeConfig.class).getConfigMapping(CredentialsConfig.class);
  }
}
