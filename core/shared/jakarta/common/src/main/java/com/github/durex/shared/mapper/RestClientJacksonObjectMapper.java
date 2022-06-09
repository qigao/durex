package com.github.durex.shared.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.ws.rs.ext.ContextResolver;

/** usage: on client interface add @RegisterProvider(RestClientJacksonObjectMapper.class) */
public class RestClientJacksonObjectMapper implements ContextResolver<ObjectMapper> {
  @Override
  public ObjectMapper getContext(Class<?> type) {
    ObjectMapper om = new ObjectMapper();
    om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return om;
  }
}
