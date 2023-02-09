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

    public static void sendResponse(String responseText, int status, HttpExchange withExchange) throws IOException {
        byte[] responseMsg = responseText.getBytes("UTF-8");
        withExchange.sendResponseHeaders(status, responseMsg.length);
        OutputStream responseStream = withExchange.getResponseBody();
        responseStream.write(responseMsg);
        responseStream.flush();
        responseStream.close();
    }
    
}
