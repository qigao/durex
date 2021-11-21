package com.github.durex;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;
import org.junit.ClassRule;
import org.testcontainers.containers.MySQLContainer;

public class MysqlResources implements QuarkusTestResourceLifecycleManager {
  @ClassRule private MySQLContainer mySQLContainer;

  @Override
  public Map<String, String> start() {
    mySQLContainer =
        (MySQLContainer)
            new MySQLContainer("mysql:8.0.19")
                .withDatabaseName("music")
                .withUsername("root")
                .withPassword("secret")
                .withConfigurationOverride("mysql-conf")
                .withEnv("MYSQL_ROOT_HOST", "%");
    mySQLContainer.start();
    System.setProperty("quarkus.datasource.db-kind", "mysql");
    System.setProperty("quarkus.datasource.jdbc.url", mySQLContainer.getJdbcUrl());
    System.setProperty("quarkus.datasource.username", mySQLContainer.getUsername());
    System.setProperty("quarkus.datasource.password", mySQLContainer.getPassword());
    return Collections.emptyMap();
  }

  @Override
  public void stop() {
    mySQLContainer.stop();
  }
}
