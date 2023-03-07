package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;

public abstract class Utils {

    /**
     * 
     * @param input InputStream josta halutaan tekstiä lukea
     * @return String-objekti tai null jos lukuoperaatiota ei voitu suorittaa
     */
    public static String read(InputStream input) {
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

    /**
     * Lähettää vastauksen määriteltyyn requestiin
     * @param responseText Vastausteksti
     * @param status Vastauksen statuskoodi
     * @param withExchange Vastauksen kohde
     * @throws IOException Jos vastausstreamiin kirjoittaessa tapahtuu virhe
     */
    public static void sendResponse(String responseText, int status, HttpExchange withExchange) {
        try {
            byte[] responseMsg = responseText.getBytes("UTF-8");
            withExchange.sendResponseHeaders(status, responseMsg.length);
            OutputStream responseStream = withExchange.getResponseBody();
            responseStream.write(responseMsg);
            responseStream.flush();
            responseStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendResponse(String responseText, int status,
                                    HttpExchange withExchange, boolean isJson) throws IOException {
        String contentType = null;
        if(isJson) {
            contentType = "application/json; charset=UTF-8";
        }
        else {
            contentType = "text/html; charset=UTF-8";
        }
        withExchange.getResponseHeaders().set("Content-Type", contentType);
        sendResponse(responseText, status, withExchange);
    }
    
}
