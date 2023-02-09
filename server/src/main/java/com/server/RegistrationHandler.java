package com.server;

import java.io.IOException;
import java.io.InputStream;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {

    private static UserAuthenticator authenticator = null;

    public RegistrationHandler(UserAuthenticator authenticator) {
        super();
        RegistrationHandler.authenticator = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                InputStream requestStream = exchange.getRequestBody();
                String requestBody = Utils.read(requestStream);
                String[] credentials = checkInput(requestBody);
                if(credentials != null) {
                    if(authenticator.registerUser(new User(credentials[0], credentials[1])))
                        Utils.sendResponse("User registered", 200, exchange);
                    else
                        Utils.sendResponse("User already exists", 405, exchange);
                }
                else {
                    Utils.sendResponse("Credentials not valid", 400, exchange);
                }
            }
            else {
                Utils.sendResponse("Not supported", 400, exchange);
            }
    }

    /**
     * Tarkistaa, onko annettu merkkijono muotoa käyttäjätunnus:salasana
     * @return null, jos syöte on vääränlainen ja String[] jos oikeanlainen
     */
    private String[] checkInput(String input) {
        String[] result = {};
        if(input.length() > 0) {
            result = input.split(":");
            if(result.length == 2)
                return result;
        }
        return null;
    }
    
}
