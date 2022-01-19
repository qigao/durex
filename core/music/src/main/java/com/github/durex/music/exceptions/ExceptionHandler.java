package com.github.durex.music.exceptions;

import com.github.durex.music.model.RespData;
import java.io.IOException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    if (exception instanceof NotFoundException) {
      return Response.status(Response.Status.OK)
          .entity(RespData.builder().result(404).msg(exception.getMessage()).data(null).build())
          .build();
    }
    if (exception instanceof IOException) {
      return Response.status(Response.Status.OK)
          .entity(RespData.builder().result(400).msg(exception.getMessage()).data(null).build())
          .build();
    }
    return Response.status(Response.Status.OK)
        .entity(RespData.builder().result(500).msg(exception.getMessage()).data(null).build())
        .build();
  }
}
