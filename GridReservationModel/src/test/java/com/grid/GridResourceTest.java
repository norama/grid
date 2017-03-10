/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grid;

import static com.grid.GridManager.getJson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.apache.openejb.testing.Classes;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author noram
 */
@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
@NotThreadSafe
public class GridResourceTest {

    private static final Logger LOGGER = Logger.getLogger("GridResourceTest");

    private static final String SERVICE_URL = "http://localhost:4204/GridResourceTest/app/";

    private static final String RESET_API_KEY = "a8742de9172f34171c086fa9c5c7090c46e570e4bc32758a55088b63a0fc77ee";
    
    @Module
    @ApplicationScoped
    @Classes(cdi = true, value = {ApplicationConfig.class, JacksonJaxbJsonProvider.class, JaxbAnnotationIntrospector.class, GridResource.class})
    public SingletonBean app() {
        //return new WebApp();
        return (SingletonBean) new SingletonBean(GridResource.class);

    }

    public GridResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws InterruptedException, UnirestException {
        // Avoid "token blacklisted" error
        Thread.sleep(1000);

        // reset grid
        resetGrid();
    }

    @After
    public void tearDown() {
    }

    private void resetGrid() throws UnirestException {
        HttpResponse<String> jsonResponse = Unirest.put(SERVICE_URL + "reset")
                .header("Content-Type", "text/plain")
                .header("accept", "text/plain")
                .body(RESET_API_KEY)
                .asString();

        assertEquals(200, jsonResponse.getStatus());
    }

    @Test
    public void get() throws Exception {

        HttpResponse<JsonNode> jsonResponse = Unirest.get(SERVICE_URL)
                .header("accept", "application/json")
                .asJson();

        JSONObject json = getJson(jsonResponse);

        JSONArray array = json.getJSONArray("cells");
        List<GridCell> cells = new ArrayList<>();
        array.forEach(a -> cells.add(new GridCell().init((JSONObject) a)));

        assertEquals(12, cells.size());
        cells.forEach(c -> assertEquals(0, c.getStatus()));
    }

    @Test
    public void reserve() throws Exception {

        GridCell cell = open(11);

        try {

            cell = reserve(cell);

        } finally {
            close(cell);
        }
    }

    @Test
    public void take() throws Exception {

        GridCell cell = open(12);

        try {

            // take from reserved state
            cell = reserve(cell);

            cell = take(cell);

            // free for next step
            cell = free(cell);

            // take from free state
            cell = take(cell);

        } finally {
            close(cell);
        }
    }

    @Test(expected = UnirestException.class)
    public void steal1() throws Exception {
              
        GridCell cell = open(13);

        try {
            cell = reserve(cell);

            cell.setTicket("invalid");

            cell = free(cell);
            
        } finally {
            close(cell);
        }
    }

    @Test(expected = UnirestException.class)
    public void steal2() throws Exception {
              
        GridCell cell = open(13);

        try {
            cell = take(cell);

            cell.setTicket("invalid");

            cell = free(cell);
            
        } finally {
            close(cell);
        }
    }

    @Test(expected = UnirestException.class)
    public void steal3() throws Exception {
              
        GridCell cell = open(13);

        try {
            cell = reserve(cell);

            cell.setTicket("invalid");

            cell = take(cell);
            
        } finally {
            close(cell);
        }
    }

    private GridCell open(int id) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SERVICE_URL + "open")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(new JSONObject().put("id", id))
                .asJson();

        JSONObject json = getJson(jsonResponse);
        LOGGER.info(json.toString(4));
        GridCell cell = new GridCell().init(json);
        assertEquals(0, cell.getStatus());
        return cell;
    }

    private GridCell close(GridCell cell) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SERVICE_URL + "close")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(new JSONObject().put("id", cell.getId()).put("token", cell.getToken()))
                .asJson();

        JSONObject json = getJson(jsonResponse);
        LOGGER.info(json.toString(4));
        return cell;
    }

    private GridCell reserve(GridCell cell) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SERVICE_URL + "reserve")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(new JSONObject().put("id", cell.getId()).put("token", cell.getToken()))
                .asJson();

        JSONObject json = getJson(jsonResponse);
        LOGGER.info(json.toString(4));
        cell = new GridCell().init(json);
        assertEquals(1, cell.getStatus());
        return cell;
    }

    private GridCell take(GridCell cell) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SERVICE_URL + "take")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(new JSONObject().put("id", cell.getId()).put("owner", "Jack").put("ticket", cell.getTicket()).put("token", cell.getToken()))
                .asJson();

        JSONObject json = getJson(jsonResponse);
        LOGGER.info(json.toString(4));
        cell = new GridCell().init(json);
        assertEquals(2, cell.getStatus());
        assertEquals("Jack", cell.getOwner());
        return cell;
    }

    private GridCell free(GridCell cell) throws Exception {        
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SERVICE_URL + "free")
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(new JSONObject().put("id", cell.getId()).put("ticket", cell.getTicket()).put("token", cell.getToken()))
                .asJson();

        JSONObject json = getJson(jsonResponse);
        LOGGER.info(json.toString(4));
        cell = new GridCell().init(json);
        assertEquals(0, cell.getStatus());
        return cell;
    }
}
