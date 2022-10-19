package com.github.durex.messaging.generator;

import java.util.Properties;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CodeGenHelper {
  public static Properties setupProperties() {
    Properties p = new Properties();
    p.setProperty("resource.loaders", "class");
    p.setProperty(
        "resource.loader.class.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    return p;
  }
}
