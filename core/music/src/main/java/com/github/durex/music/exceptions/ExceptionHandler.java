package com.github.durex.music.exceptions;

import com.github.durex.music.model.RespData;
import java.io.IOException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@SuppressWarnings("rawtypes")
public class ExceptionHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    if (exception instanceof NotFoundException) {
      return Response.status(Response.Status.OK)
          .entity(new RespData(404, exception.getMessage(), ""))
          .build();
    }
    if (exception instanceof IOException) {
      return Response.status(Response.Status.OK)
          .entity(new RespData(400, exception.getMessage(), ""))
          .build();
    }
    return Response.status(Response.Status.OK)
        .entity(new RespData(500, "Unknown Error", ""))
        .build();
  }
}
