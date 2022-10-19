package com.github.durex.messaging.generator;

import com.github.durex.messaging.generator.model.CodeNameInfo;
import com.github.durex.messaging.generator.model.ServiceInfo;
import com.github.durex.messaging.generator.model.TopicInfo;
import java.io.Writer;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class RedisCodeGenerator {
  VelocityEngine velocityEngine;

  public RedisCodeGenerator() {
    Properties p = CodeGenHelper.setupProperties();
    velocityEngine = new VelocityEngine(p);
  }

  public void listenerCodeGenerate(CodeNameInfo codeNameInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/SubEventListener.vm");
    var context = buildVelocityContext(codeNameInfo);
    template.merge(context, writer);
  }

  private VelocityContext buildVelocityContext(CodeNameInfo codeNameInfo) {
    var context = new VelocityContext();
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

  public void subEventRunnerCodeGenerator(TopicInfo annotations, Writer writer) {
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
    var template = velocityEngine.getTemplate("templates/stream/SubStreamExecutor.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }

  public void taskCodeGenerator(TopicInfo topicInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/stream/SubStreamTask.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }

  public void handlerCodeGenerator(TopicInfo topicInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/stream/SubStreamHandler.vm");
    var context = buildLifeCycleContext(topicInfo);
    template.merge(context, writer);
  }

  public void daemonCodeGenerator(ServiceInfo serviceInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/QuarkusDaemon.vm");
    var context = buildServiceContext(serviceInfo);
    template.merge(context, writer);
  }

  private VelocityContext buildServiceContext(ServiceInfo serviceInfo) {
    var context = new VelocityContext();
    context.put("packageName", serviceInfo.getPackageName());
    context.put("className", serviceInfo.getClassName());
    context.put("interfaceName", serviceInfo.getInterfaceName());
    context.put("simpleInterfaceName", serviceInfo.getSimpleInterfaceName());
    return context;
  }

  public void lifeCycleCodeGenerator(ServiceInfo serviceInfo, Writer writer) {
    var template = velocityEngine.getTemplate("templates/RemoteServiceLifecycle.vm");
    var context = buildServiceContext(serviceInfo);
    template.merge(context, writer);
  }
}
