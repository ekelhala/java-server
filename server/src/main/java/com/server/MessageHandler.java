package com.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.*;

public class MessageHandler implements HttpHandler{
    
    private List<String> messages = new ArrayList<>();

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
                        exchange.sendResponseHeaders(500, -1);
                    }
                    break;
                default:
                    exchange.sendResponseHeaders(400, -1);
                    break;
            }
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 
     * @param exchange HttpExchange jossa on haluttu viesti.
     * @return Totuusarvo, joka kertoo onnistuiko viestin lisäys serverille.
     */
    private boolean handlePost(HttpExchange exchange) {
        InputStream postStream = exchange.getRequestBody();
        String message = Utils.read(postStream);
        if(message != null) {
            messages.add(message);
            return true;
        }
        return false;
    }

    private void handleGet(HttpExchange exchange) {
        StringBuilder resultBuilder = new StringBuilder();
        if(messages.isEmpty()) {
            resultBuilder.append("No messages!");
        }
        else {
            for(String message : messages) {
                resultBuilder.append(message);            
            }
        }
        try {
            Utils.sendResponse(resultBuilder.toString(), 200, exchange);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
