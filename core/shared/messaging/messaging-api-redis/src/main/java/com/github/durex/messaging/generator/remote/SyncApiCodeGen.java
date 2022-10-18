package com.github.durex.messaging.generator.remote;

import com.github.durex.messaging.generator.model.MethodInfo;
import com.github.durex.messaging.processor.Helper;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import javax.annotation.processing.Filer;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class SyncApiCodeGen implements ApiCodeGenStrategy {
  private List<MethodInfo> methodInfos;
  VelocityEngine velocityEngine;

  @Override
  public void filter(List<MethodInfo> methodInfoList) {
    methodInfos = methodInfoList;
  }

  @Override
  @SneakyThrows
  public void generate(String packageName, String className, Filer filer) {
    var destClassName = packageName + ".R" + Helper.getSimpleName(className);
    final Writer writer = filer.createSourceFile(destClassName).openWriter();
    var template = velocityEngine.getTemplate("templates/RemoteServiceApi.vm");
    var context = new VelocityContext();
    context.put("packageName", packageName);
    context.put("className", Helper.getSimpleName(className));
    context.put("methodInfos", methodInfos);
    template.merge(context, writer);
    writer.close();
  }

  public SyncApiCodeGen() {
    velocityEngine = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loaders", "class");
    p.setProperty(
        "resource.loader.class.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init(p);
  }
}
