package com.grid;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Nora Mate
 */
public class GridManager {

    private static final Logger LOGGER = Logger.getLogger("GridManager");

    private static final String DF_API_KEY_NAME = "X-DreamFactory-Api-Key";
    private static final String DF_API_KEY_VALUE = "36fda24fe5588fa4285ac6c6c2fdfbdb6b6bc9834699774c9bf777f706d05a88";

    private static final String DF_SESSION_TOKEN_NAME = "X-DreamFactory-Session-Token";

    private static final String HOST = ApplicationContext.getDreamFactoryUrl();
    private static final String ROOT = "/api/v2";

    private static final String SESSION = "/user/session";
    private static final String GRID = "/pg/_table/grid";

    static JSONObject loginData(int seconds) throws JSONException {
        return new JSONObject()
                .put("email", "smtp.dreamfactory@gmail.com")
                .put("password", "pguser")
                .put("duration", seconds);
    }

    static JSONObject getJson(JsonNode body) throws UnirestException, JSONException {
        JSONObject json = body.getObject();
        //LOGGER.info(json.toString(4));
        if (json.has("error")) {
            throw new UnirestException(json.getJSONObject("error").getString("message"));
        }
        return json;
    }

    public String openSession() throws UnirestException, JSONException {
        return openSession(0);
    }

    public String openSession(int seconds) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(HOST + ROOT + SESSION)
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .body(loginData(seconds))
                .asJson();

        JSONObject json = getJson(jsonResponse.getBody());
        Assert.isTrue(json.has("session_token"), "session_token is missing:\n" + json.toString(4));
        return json.getString("session_token");
    }
    
    public String refreshToken(String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.put(HOST + ROOT + SESSION)
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .asJson();

        JSONObject json = getJson(jsonResponse.getBody());
        Assert.isTrue(json.has("session_token"), "session_token is missing:\n" + json.toString(4));
        return json.getString("session_token");       
    }

    public void closeSession(String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.delete(HOST + ROOT + SESSION)
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .asJson();

        JSONObject json = getJson(jsonResponse.getBody());
        Assert.isTrue(json.has("success"), "Could not close session:\n" + json.toString(4));
        Assert.isTrue(json.getBoolean("success"), "Could not close session:\n" + json.toString(4));
    }

    private GridCell get(int cellId, String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.get(HOST + ROOT + GRID + '/' + cellId)
                .queryString("fields", "*")
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .asJson();

        return new GridCell().init(getJson(jsonResponse.getBody()));
    }

    GridCell update(int cellId, JSONObject changes, String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.patch(HOST + ROOT + GRID + '/' + cellId)
                .queryString("fields", "*")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .body(changes)
                .asJson();

        return new GridCell().init(getJson(jsonResponse.getBody()));
    }

    Grid getGrid(String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.get(HOST + ROOT + GRID)
                .queryString("fields", "id, row, col, status, owner")
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .asJson();

        JSONObject json = getJson(jsonResponse.getBody());
        return toGrid(json);
    }

    Grid toGrid(JSONObject json) throws JSONException {
        Assert.isTrue(json.has("resource"), "Missing key: 'resource', JSON: " + json.toString());
        JSONArray jsonCells = json.getJSONArray("resource");
        int size = jsonCells.length();
        List<GridCell> cells = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            cells.add(new GridCell().init(jsonCells.getJSONObject(i)));
        }
        return new Grid(cells);
    }

    GridCell reserve(GridCell clientCell, String token) throws UnirestException, JSONException {
        int cellId = clientCell.getId();
        GridCell cell = get(cellId, token);
        if (cell.getStatus() == 0) {
            String ticket = UUID.randomUUID().toString();
            return update(cellId, new JSONObject().put("status", 1).put("ticket", ticket), token);
        } else {
            throw new IllegalStateException("Cell " + cellId + " is not free.");
        }
    }

    GridCell take(GridCell clientCell, String token) throws UnirestException, JSONException {
        int cellId = clientCell.getId();
        GridCell cell = get(cellId, token);
        int status = cell.getStatus();
        if (status == 2) {
            throw new IllegalStateException("Cell " + cellId + " has already been taken.");
        }
        String owner = clientCell.getOwner();
        if (owner == null || owner.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner should not be empty!");
        }
        JSONObject changes = new JSONObject().put("status", 2).put("owner", owner.trim());
        if (status == 0) {
            String ticket = UUID.randomUUID().toString();
            changes.put("ticket", ticket);
        }
        return update(cellId, changes, token);
    }

    GridCell validate(GridCell clientCell, String token) throws UnirestException, JSONException {
        int cellId = clientCell.getId();
        GridCell cell = get(cellId, token);
        if (validTicket(cell, clientCell)) {
            return clientCell;
        } else {
            throw new IllegalStateException("Cell " + cellId + " ticket mismatch.");
        }
    }

    static boolean validTicket(GridCell cell, GridCell clientCell) {
        return cell.getTicket() != null && cell.getTicket().equals(clientCell.getTicket());
    }

    GridCell free(GridCell clientCell, String token) throws UnirestException, JSONException {
        int cellId = clientCell.getId();
        GridCell cell = get(cellId, token);
        if (validTicket(cell, clientCell)) {
            return update(cellId, new JSONObject().put("status", 0).put("ticket", JSONObject.NULL).put("owner", JSONObject.NULL).put("remark", JSONObject.NULL), token);
        } else {
            throw new IllegalStateException("Invalid ticket for cell " + cellId + ".");
        }

    }

    public void freeExpiredCells(String token) throws UnirestException, JSONException {
        HttpResponse<JsonNode> jsonResponse = Unirest.get(HOST + ROOT + GRID)
                .queryString("fields", "id, ticket, modified")
                .queryString("filter", "status = 1")
                .header("accept", "application/json")
                .header(DF_API_KEY_NAME, DF_API_KEY_VALUE)
                .header(DF_SESSION_TOKEN_NAME, token)
                .asJson();

        JSONObject json = getJson(jsonResponse.getBody());
        freeCells(toGrid(json), token);
    }

    private void freeCells(Grid grid, String token) throws UnirestException, JSONException {

        long expiration = ApplicationContext.getExpiration();
        LOGGER.info("expiration time (mins): " + expiration);

        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");
        for (GridCell cell : grid.getCells()) {
            ZonedDateTime modified = ZonedDateTime.parse(cell.getModified(), formatter);
            if (now.isAfter(modified.plusMinutes(expiration))) {
                free(cell, token);
                LOGGER.info("Expired cell: " + cell.getId() + " is now free.");
            }
        }
    }

}
