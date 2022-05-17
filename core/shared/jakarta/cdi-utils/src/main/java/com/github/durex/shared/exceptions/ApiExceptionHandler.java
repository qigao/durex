package com.github.durex.shared.exceptions;

import com.github.durex.shared.api.RespData;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionHandler implements ExceptionMapper<ApiException> {

  @Override
  public Response toResponse(ApiException exception) {
    return Response.ok()
        .entity(RespData.builder().error(exception.getErrorResponse()).build())
        .build();
  }
}
