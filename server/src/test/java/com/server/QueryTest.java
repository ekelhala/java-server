package com.server;

import org.json.JSONObject;
import org.junit.*;

import org.json.*;

import com.server.Query.InvalidQueryTypeException;

public class QueryTest {
    
    @Test(expected = InvalidQueryTypeException.class)
    public void testInvalidType() throws InvalidQueryTypeException {
        Query.verifyQueryType("wrong");
    }

    @Test(expected = JSONException.class)
    public void testInvalidJSON() throws InvalidQueryTypeException {
        Query.fromJSON(new JSONObject());
    }

}
