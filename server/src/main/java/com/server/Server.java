package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.net.httpserver.*;

public class Server implements HttpHandler{

    private static HttpServer server;
    private List<String> messages = new ArrayList<>();
    public static void main( String[] args )
    {
        try {
            server = HttpServer.create(new InetSocketAddress(8001), 0);
        }
        catch(IOException exception) {
            exception.printStackTrace();
        }
        server.createContext("/warning", new Server());
        server.setExecutor(null);
        server.start();
        System.out.println( "Server online!" );
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
     * @param exchange HttpExchange jossa on haluttu viesti
     * @return Totuusarvo, joka kertoo onnistuiko viestin lisäys serverille.
     */
    private boolean handlePost(HttpExchange exchange) {
        InputStream postStream = exchange.getRequestBody();
        String message = read(postStream);
        if(message != null) {
            messages.add(message);
            return true;
        }
        return false;
    }

    private void handleGet(HttpExchange exchange) {
        StringBuilder resultBuilder = new StringBuilder();
        if(messages.isEmpty()) {
            resultBuilder.append("No messages");
        }
        else {
            for(String message : messages) {
                resultBuilder.append(message);            
            }
        }
        try {
            byte[] responseBytes = resultBuilder.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
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
