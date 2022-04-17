package com.github.durex.jooq;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Slf4j
@Singleton
public final class DslContextProducer {
  @ConfigProperty(name = "quarkus.datasource.jdbc.url")
  private String databaseUrl;

  @ConfigProperty(name = "quarkus.datasource.username")
  private String databaseUser;

  @ConfigProperty(name = "quarkus.datasource.password")
  private String databasePassword;

  @ConfigProperty(name = "quarkus.datasource.jdbc.max-size", defaultValue = "5")
  private int maxPoolSize;

  @Produces
  @ApplicationScoped
  public DSLContext getDslContext() {
    try {
      return DSL.using(getConfiguration());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HikariDataSource getHikariDataSource() {

    HikariConfig hc = new HikariConfig();
    hc.setUsername(databaseUser);
    hc.setPassword(databasePassword);
    hc.setJdbcUrl(databaseUrl);
    hc.setMaximumPoolSize(maxPoolSize);
    return new HikariDataSource(hc);
  }

  private Configuration getConfiguration() {
    var settings =
        new Settings()
            .withExecuteLogging(true)
            .withRenderFormatted(true)
            .withRenderCatalog(false)
            .withRenderSchema(false)
            .withReturnIdentityOnUpdatableRecord(false)
            .withRenderOutputForSQLServerReturningClause(true)
            .withMaxRows(1000)
            .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
            .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED);

    return new DefaultConfiguration().set(getHikariDataSource()).set(settings);
  }
}
