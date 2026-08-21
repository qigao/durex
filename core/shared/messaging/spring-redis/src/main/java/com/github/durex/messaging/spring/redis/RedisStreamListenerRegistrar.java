package com.github.durex.messaging.spring.redis;

import com.github.durex.messaging.annotation.RedisStreamListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ReflectionUtils;
import tools.jackson.databind.json.JsonMapper;

public class RedisStreamListenerRegistrar implements BeanPostProcessor, SmartLifecycle {
  private final RedisConnectionFactory connectionFactory;
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final Environment environment;
  private final List<Handler> handlers = new ArrayList<>();

  private volatile StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
  private volatile boolean running;

  public RedisStreamListenerRegistrar(
      RedisConnectionFactory connectionFactory,
      StringRedisTemplate redisTemplate,
      JsonMapper jsonMapper,
      Environment environment) {
    this.connectionFactory = connectionFactory;
    this.redisTemplate = redisTemplate;
    this.jsonMapper = jsonMapper;
    this.environment = environment;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> targetClass = AopUtils.getTargetClass(bean);
    Map<Method, RedisStreamListener> methods =
        MethodIntrospector.selectMethods(
            targetClass,
            (MethodIntrospector.MetadataLookup<RedisStreamListener>)
                method ->
                    AnnotatedElementUtils.findMergedAnnotation(method, RedisStreamListener.class));
    methods.forEach((method, annotation) -> handlers.add(new Handler(bean, method, annotation)));
    return bean;
  }

  @Override
  public synchronized void start() {
    if (running) {
      return;
    }

    var listenerContainer = StreamMessageListenerContainer.create(connectionFactory);
    handlers.forEach(handler -> register(listenerContainer, handler));
    listenerContainer.start();
    container = listenerContainer;
    running = true;
  }

  private void register(
      StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
      Handler handler) {
    Method method = AopUtils.selectInvocableMethod(handler.method(), handler.bean().getClass());
    if (method.getParameterCount() != 1) {
      throw new IllegalStateException(
          "@RedisStreamListener method must declare exactly one payload parameter: " + method);
    }
    ReflectionUtils.makeAccessible(method);

    RedisStreamListener annotation = handler.annotation();
    String stream = environment.resolvePlaceholders(annotation.stream());
    String group = environment.resolvePlaceholders(annotation.group());
    String consumerName = environment.resolvePlaceholders(annotation.consumer());
    Consumer consumer = Consumer.from(group, consumerName);
    StreamOffset<String> offset = StreamOffset.create(stream, ReadOffset.lastConsumed());

    StreamListener<String, MapRecord<String, String, String>> listener =
        record -> invoke(handler.bean(), method, annotation, stream, group, record);

    if (annotation.autoAck()) {
      listenerContainer.receiveAutoAck(consumer, offset, listener);
    } else {
      listenerContainer.receive(consumer, offset, listener);
    }
  }

  private void invoke(
      Object bean,
      Method method,
      RedisStreamListener annotation,
      String stream,
      String group,
      MapRecord<String, String, String> record) {
    String payload = record.getValue().get("payload");
    if (payload == null) {
      throw new IllegalStateException("Redis Stream record does not contain required 'payload' field");
    }

    try {
      Object value = jsonMapper.readValue(payload, method.getParameterTypes()[0]);
      method.invoke(bean, value);
      if (!annotation.autoAck()) {
        redisTemplate.opsForStream().acknowledge(stream, group, record.getId());
      }
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      throw new IllegalStateException("Redis Stream listener invocation failed: " + method, target);
    } catch (Exception e) {
      throw new IllegalStateException("Redis Stream listener invocation failed: " + method, e);
    }
  }

  @Override
  public synchronized void stop() {
    StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer =
        container;
    if (listenerContainer != null) {
      listenerContainer.stop();
      container = null;
    }
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  private record Handler(Object bean, Method method, RedisStreamListener annotation) {}
}
