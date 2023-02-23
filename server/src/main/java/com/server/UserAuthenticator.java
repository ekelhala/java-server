package com.server;

import java.sql.SQLException;

import com.sun.net.httpserver.*;

public class UserAuthenticator extends BasicAuthenticator {

    private static MessageDB db;

    public UserAuthenticator() {
        super("warning");
        db = MessageDB.getInstance();
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        try {
            return db.validateUser(username, password);
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(User withUser) {
        try {
            return db.addNewUser(withUser);
        }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
