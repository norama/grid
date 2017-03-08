package com.grid;

import com.mashape.unirest.http.exceptions.UnirestException;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author Nora Mate
 */
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@ApplicationScoped
@Path("app")
public class GridResource {

    @Context
    private UriInfo context;

    private final GridManager gridManager;

    /**
     * Creates a new instance of GridManager
     */
    public GridResource() {
        gridManager = new GridManager();
    }

    @GET
    @Lock(LockType.READ)
    @Produces(MediaType.APPLICATION_JSON)
    public Grid getGrid() throws UnirestException, JSONException {
        String token = gridManager.openSession();

        try {
            return gridManager.getGrid(token);
        } finally {
            gridManager.closeSession(token);
        }
    }
    
    @POST
    @Path("open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(GridCell clientCell) throws UnirestException, JSONException {
        String token = gridManager.openSession();
        return response(clientCell, token);
    }    

    @POST
    @Path("close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response close(GridCell clientCell) throws UnirestException, JSONException {
        String token = clientCell.getToken();
        if (token != null) {
            gridManager.closeSession(token);
            clientCell.setToken(null);
            return Response.ok(clientCell).build();
        } else {
            throw new IllegalArgumentException("No token stored in cell.");
        }
    }    

    @POST
    @Lock(LockType.WRITE)
    @Path("reserve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reserve(GridCell clientCell) throws UnirestException, JSONException {
        String clientToken = clientCell.getToken();
        String token = clientToken == null ? gridManager.openSession() : clientToken;
        try {
            GridCell cell = gridManager.reserve(clientCell, token);
            return response(cell, clientToken);
        } finally {
            if (clientToken == null) {
                gridManager.closeSession(token);
            }
        }
    }

    @POST
    @Lock(LockType.WRITE)
    @Path("take")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response take(GridCell clientCell) throws UnirestException, JSONException {
        String clientToken = clientCell.getToken();
        String token = clientToken == null ? gridManager.openSession() : clientToken;
        try {
            GridCell cell = gridManager.take(clientCell, token);
            return response(cell, clientToken);
        } finally {
            if (clientToken == null) {
                gridManager.closeSession(token);
            }
        }
    }

    @POST
    @Lock(LockType.WRITE)
    @Path("free")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response free(GridCell clientCell) throws UnirestException, JSONException {
        String clientToken = clientCell.getToken();
        String token = clientToken == null ? gridManager.openSession() : clientToken;
        try {
            GridCell cell = gridManager.free(clientCell, token);
            return response(cell, clientToken);
        } finally {
            if (clientToken == null) {
                gridManager.closeSession(token);
            }
        }
    }

    @POST
    @Lock(LockType.READ)
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validate(GridCell clientCell) throws UnirestException, JSONException {
        String clientToken = clientCell.getToken();
        String token = clientToken == null ? gridManager.openSession() : clientToken;
        try {
            GridCell cell = gridManager.validate(clientCell, token);
            return response(cell, clientToken);
        } finally {
            if (clientToken == null) {
                gridManager.closeSession(token);
            }
        }
    }
    
    private static Response response(GridCell cell, String token) {
        return Response.ok(cell.setToken(token)).build();
    }
}
