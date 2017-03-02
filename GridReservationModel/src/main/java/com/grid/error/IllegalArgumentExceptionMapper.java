package com.grid.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nora Mate
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    
    @Override
    public Response toResponse(IllegalArgumentException e) {
        return Response.serverError().entity(new GridManagerError(e)).build();
    }
}
