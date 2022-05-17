package com.github.durex.jooq;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

@Slf4j
@Singleton
public final class DslContextProducer {
  @ConfigProperty(name = "datasource.r2dbc.url")
  private String r2dbcUrl;

  @ConfigProperty(name = "datasource.jdbc.url", defaultValue = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
  private String jdbcUrl;

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
      if (r2dbcUrl != null) {
        log.info("Using R2DBC connection {}", r2dbcUrl);
        return DSL.using(getConnectionFactory(), SQLDialect.MYSQL, getSettings());
      } else {
        log.info("Using JDBC connection {}", jdbcUrl);
        return DSL.using(getConfiguration());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private HikariDataSource getHikariDataSource() {
    var hikariConfig = new HikariConfig();
    hikariConfig.setUsername(databaseUser);
    hikariConfig.setPassword(databasePassword);
    hikariConfig.setJdbcUrl(jdbcUrl);
    hikariConfig.setMaximumPoolSize(maxPoolSize);
    return new HikariDataSource(hikariConfig);
  }

  private Configuration getConfiguration() {
    Settings settings = getSettings();
    return new DefaultConfiguration().set(getHikariDataSource()).set(settings);
  }

  private Settings getSettings() {
    return new Settings()
        .withExecuteLogging(true)
        .withRenderFormatted(true)
        .withRenderCatalog(false)
        .withRenderSchema(false)
        .withReturnIdentityOnUpdatableRecord(false)
        .withRenderOutputForSQLServerReturningClause(true)
        .withMaxRows(1000)
        .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
        .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED);
  }

  private ConnectionFactory getConnectionFactory() {
    return ConnectionFactories.get(
        ConnectionFactoryOptions.parse(r2dbcUrl)
            .mutate()
            .option(ConnectionFactoryOptions.USER, databaseUser)
            .option(ConnectionFactoryOptions.PASSWORD, databasePassword)
            .build());
  }
}
