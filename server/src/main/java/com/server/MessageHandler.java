package com.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
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
                    if(handlePost(exchange)) {
                        exchange.sendResponseHeaders(200, -1);
                    }
                    else {
                        Utils.sendResponse("Request body not valid JSON", 400, exchange);
                    }
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
    private boolean handlePost(HttpExchange exchange) throws SQLException {
        InputStream postStream = exchange.getRequestBody();
        String message = Utils.read(postStream);
        try {
            WarningMessage addMessage = WarningMessage.fromJSON(new JSONObject(message));
            db.addNewMessage(addMessage);
        }
        catch(Exception exception) {
            return false;
        }
        return true;
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
