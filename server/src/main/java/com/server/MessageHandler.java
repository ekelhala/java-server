package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            /*
            byte[] responseBytes = resultBuilder.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream responseOutputStream = exchange.getResponseBody();
            responseOutputStream.write(responseBytes);
            responseOutputStream.flush();
            responseOutputStream.close();*/
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param input InputStream josta halutaan tekstiä lukea
     * @return String-objekti tai null jos lukuoperaatiota ei voitu suorittaa
     */
    private String read(InputStream input) {
        String result = null;
        try {
        InputStreamReader reader = new InputStreamReader(input);
        result = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
         reader.close();
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
        return result;
    }
}
