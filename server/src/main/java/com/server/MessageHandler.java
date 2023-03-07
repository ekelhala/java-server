package com.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class MessageHandler implements HttpHandler{
    
    private MessageDB db;

    public MessageHandler() {
        super();
        db = MessageDB.getInstance();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            switch(exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    Utils.sendResponse("Not supported", 400, exchange);
                    break;
            }
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 
     * @param exchange HttpExchange jossa on haluttu viesti.
     * @return Totuusarvo, joka kertoo onnistuiko viestin lisäys serverille.
     */
    private void handlePost(HttpExchange exchange) throws SQLException {
        InputStream postStream = exchange.getRequestBody();
        String message = Utils.read(postStream);
        JSONObject obj = null;
        try {
            obj = new JSONObject(message);
        }
        catch(JSONException exception) {
            exception.printStackTrace();
            Utils.sendResponse("Request body not valid JSON", 400, exchange);
        }
        if(obj.has("query")) {
            Query query = null;
            try {
                query = Query.fromJSON(new JSONObject(message));
            }
            catch(JSONException exception) {
                Utils.sendResponse("Query parameter not valid", 400, exchange);
            }
            List<WarningMessage> queryResults = db.queryMessages(query);
                JSONArray array = new JSONArray();
                for(WarningMessage queryResult : queryResults) {
                    array.put(queryResult.toJSON());
                }
                String queryResponse = array.toString();
                Utils.sendResponse(queryResponse, 200, exchange);
        }
        else {
            WarningMessage addMessage = null;
            try {
                addMessage = WarningMessage.fromJSON(new JSONObject(message));
            }
            catch(Exception exception) {
                exception.printStackTrace();
                Utils.sendResponse("Request body not valid JSON", 400, exchange);
            }
            db.addNewMessage(addMessage);
            try {
                exchange.sendResponseHeaders(200, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleGet(HttpExchange exchange) throws SQLException {
        StringBuilder resultBuilder = new StringBuilder();
        List<WarningMessage> messages = db.getAllMessages();
        if(!messages.isEmpty()) {
            JSONArray array = new JSONArray();
            for(WarningMessage message : messages) {
                array.put(message.toJSON());
            }
            resultBuilder.append(array.toString());
            try {
                Utils.sendResponse(resultBuilder.toString(), 200, exchange, true);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                exchange.sendResponseHeaders(204, -1);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
