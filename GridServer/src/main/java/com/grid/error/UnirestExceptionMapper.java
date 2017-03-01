package com.grid.error;

import com.mashape.unirest.http.exceptions.UnirestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nora Mate
 */
@Provider
public class UnirestExceptionMapper implements ExceptionMapper<UnirestException> {
    
    @Override
    public Response toResponse(UnirestException e) {
        return Response.serverError().entity(new GridManagerError(e)).build();
    }
}
