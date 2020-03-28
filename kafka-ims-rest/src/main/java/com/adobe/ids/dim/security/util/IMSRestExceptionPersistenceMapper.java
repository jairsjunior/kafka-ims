package com.adobe.ids.dim.security.util;

import com.adobe.ids.dim.security.common.exception.IMSRestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IMSRestExceptionPersistenceMapper implements ExceptionMapper<IMSRestException> {
    @Override
    public Response toResponse(IMSRestException exception) {
        return Response.status(exception.getStatus()).entity(exception).build();
    }
}
