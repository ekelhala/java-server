package com.server;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.*;

public class UserAuthenticator extends BasicAuthenticator {

    private static List<User> users = null;

    public UserAuthenticator() {
        super("warning");
        users = new ArrayList<User>();
        users.add(new User("matti", "salasana", "matti@matti.com"));
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        for(User findUser : users) {
            if(findUser.getUsername().equals(username) && findUser.getPassword().equals(password))
                return true;
        }
        return false;
    }

    public boolean registerUser(User withUser) {
        for(User checkUser : users) {
            if(checkUser.getUsername().equals(withUser.getUsername())) {
                return false;
            }
        }
        users.add(withUser);
        return true;
    }
    
}
