package com.github.durex.jooq;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

@Slf4j
public final class DslContextProducer {
  @ConfigProperty(name = "datasource.jdbc.url")
  private String databaseUrl;

  @ConfigProperty(name = "datasource.username")
  private String databaseUser;

  @ConfigProperty(name = "datasource.password")
  private String databasePassword;

  @ConfigProperty(name = "datasource.jdbc.max-size", defaultValue = "5")
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
    var hikariConfig = new HikariConfig();
    hikariConfig.setUsername(databaseUser);
    hikariConfig.setPassword(databasePassword);
    hikariConfig.setJdbcUrl(databaseUrl);
    hikariConfig.setMaximumPoolSize(maxPoolSize);
    return new HikariDataSource(hikariConfig);
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
