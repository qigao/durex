package com.github.durex.messaging.generator;

import com.github.durex.messaging.generator.model.CodeNameInfo;
import com.github.durex.messaging.generator.model.TopicInfo;
import java.io.Writer;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class RedisCodeGenerator {
  VelocityEngine velocityEngine;

  public RedisCodeGenerator() {
    velocityEngine = new VelocityEngine();
    Properties p = new Properties();
    p.setProperty("resource.loaders", "class");
    p.setProperty(
        "resource.loader.class.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init(p);
  }

  public void listenerCodeGenerate(CodeNameInfo codeNameInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubEventListener.vm");
    var context = buildVelocityContext(codeNameInfo);
    template.merge(context, writer);
  }

  private VelocityContext buildVelocityContext(CodeNameInfo codeNameInfo) {
    VelocityContext context = new VelocityContext();
    fillContext(codeNameInfo, context);
    return context;
  }

  private static void fillContext(CodeNameInfo codeNameInfo, VelocityContext context) {
    context.put("packageName", codeNameInfo.getPackageName());
    context.put("className", codeNameInfo.getClassName());
    context.put("methodName", codeNameInfo.getMethodName());
    context.put("paramType", codeNameInfo.getParamType());
    context.put("simpleClassName", codeNameInfo.getSimpleClassName());
    context.put("simpleParamType", codeNameInfo.getSimpleParamType());
  }

  public void lifecycleCodeGenerator(TopicInfo annotations, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubEventRunner.vm");
    var context = buildLifeCycleContext(annotations);
    template.merge(context, writer);
  }

  private VelocityContext buildLifeCycleContext(TopicInfo topicInfo) {
    VelocityContext context = new VelocityContext();
    fillContext(topicInfo.getCodeNameInfo(), context);
    context.put("topicName", topicInfo.getValue());
    context.put("codec", topicInfo.getCodec());
    context.put("groupName", topicInfo.getGroup());
    context.put("subscriber", topicInfo.getSubscriber());
    return context;
  }

  public void executorCodeGenerator(TopicInfo topicInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubStreamExecutor.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }

  public void taskCodeGenerator(TopicInfo topicInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubStreamTask.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }

  public void handlerCodeGenerator(TopicInfo topicInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubStreamHandler.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }
}
