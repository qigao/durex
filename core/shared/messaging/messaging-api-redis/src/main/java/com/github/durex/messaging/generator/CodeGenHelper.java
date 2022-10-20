package com.github.durex.messaging.generator;

import java.util.Properties;
import lombok.experimental.UtilityClass;
import org.apache.velocity.app.VelocityEngine;

@UtilityClass
public class CodeGenHelper {
  public static VelocityEngine initVelocity() {
    var velocityEngine = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loaders", "class");
    p.setProperty(
        "resource.loader.class.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init(p);
    return velocityEngine;
  }
}
