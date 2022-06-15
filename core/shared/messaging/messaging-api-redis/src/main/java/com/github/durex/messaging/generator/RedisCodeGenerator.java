package com.github.durex.messaging.generator;

import com.github.durex.messaging.generator.model.Annotations;
import com.github.durex.messaging.generator.model.PlainAnnotations;
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

  public void listenerCodeGenerate(Annotations templateField, Writer writer) {
    var template = velocityEngine.getTemplate("templates/InComingMessageListener.vm");
    var context = buildVelocityContext(templateField);
    template.merge(context, writer);
  }

  private VelocityContext buildVelocityContext(Annotations templateField) {
    VelocityContext context = new VelocityContext();
    fillContext(templateField, context);
    return context;
  }

  private static void fillContext(Annotations templateField, VelocityContext context) {
    context.put("packageName", templateField.getPackageName());
    context.put("className", templateField.getClassName());
    context.put("methodName", templateField.getMethodName());
    context.put("paramType", templateField.getParamType());
    context.put("simpleClassName", templateField.getSimpleClassName());
    context.put("simpleParamType", templateField.getSimpleParamType());
  }

  public void lifecycleCodeGenerator(PlainAnnotations annotations, Writer writer) {
    var template = velocityEngine.getTemplate("templates/RegisterMessageListener.vm");
    var context = buildLifeCycleContext(annotations);
    template.merge(context, writer);
  }

  private VelocityContext buildLifeCycleContext(PlainAnnotations annotations) {
    VelocityContext context = new VelocityContext();
    fillContext(annotations, context);
    context.put("topicName", annotations.getValue());
    context.put("codec", annotations.getCodec());
    context.put("subscriber", annotations.getSubscriber());
    return context;
  }
}
