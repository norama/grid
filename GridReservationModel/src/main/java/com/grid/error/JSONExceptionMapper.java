package com.grid.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.json.JSONException;

/**
 *
 * @author Nora Mate
 */
@Provider
public class JSONExceptionMapper implements ExceptionMapper<JSONException> {
    
    @Override
    public Response toResponse(JSONException e) {
        return Response.serverError().entity(new GridManagerError(e)).build();
    }
}
