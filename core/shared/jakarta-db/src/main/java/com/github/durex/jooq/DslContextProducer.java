package com.github.durex.jooq;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
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
@ApplicationScoped
public final class DslContextProducer {
  @ConfigProperty(name = "datasource.r2dbc.url")
  Optional<String> r2dbcUrl;

  @ConfigProperty(name = "datasource.jdbc.url")
  Optional<String> jdbcUrl;

  @ConfigProperty(name = "datasource.username", defaultValue = "")
  String databaseUser;

  @ConfigProperty(name = "datasource.password", defaultValue = "")
  String databasePassword;

  @ConfigProperty(name = "datasource.jdbc.maxPoolsize", defaultValue = "5")
  int maxPoolSize;

  @Produces
  @ApplicationScoped
  public DSLContext getDslContext() {
    if (r2dbcUrl.isPresent()) {
      log.info("Using R2DBC connection {}", r2dbcUrl);
      return DSL.using(getConnectionFactory(), SQLDialect.MYSQL, getSettings());
    } else if (jdbcUrl.isPresent()) {
      log.info("Using JDBC connection {}", jdbcUrl);
      return DSL.using(getConfiguration());
    } else {
      log.error("No datasource configured, please check your configuration");
      throw new IllegalStateException("No datasource configured");
    }
  }

  private HikariDataSource getHikariDataSource() {
    var hikariConfig = new HikariConfig();
    hikariConfig.setUsername(databaseUser);
    hikariConfig.setPassword(databasePassword);
    hikariConfig.setJdbcUrl(jdbcUrl.orElse(""));
    hikariConfig.setMaximumPoolSize(maxPoolSize);
    hikariConfig.setConnectionTimeout(30000);

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
        ConnectionFactoryOptions.parse(r2dbcUrl.orElse(""))
            .mutate()
            .option(ConnectionFactoryOptions.USER, databaseUser)
            .option(ConnectionFactoryOptions.PASSWORD, databasePassword)
            .build());
  }
}
