package com.server;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {

    private UserAuthenticator authenticator = null;
    private Utils utils = new Utils(); 

    public RegistrationHandler(UserAuthenticator authenticator) {
        super();
        this.authenticator = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                InputStream requestStream = exchange.getRequestBody();
                String requestBody = utils.read(requestStream);
                User addUser = null;
                try {
                    addUser = User.fromJSON(new JSONObject(requestBody));
                    if(addUser.getUsername().equals("") || addUser.getPassword().equals("")
                        || addUser.getEmail().equals("")) {
                            utils.sendResponse("User credentials not valid", 400, exchange);
                    }
                    else {
                        if(authenticator.registerUser(addUser))
                            utils.sendResponse("User registered", 200, exchange);
                        else
                            utils.sendResponse("User already exists", 405, exchange);
                    }
                }
                catch(JSONException exception) {
                    utils.sendResponse("Request body not valid JSON", 400, exchange);
                }
            }
            else {
                utils.sendResponse("Not supported", 400, exchange);
            }
    }
}
