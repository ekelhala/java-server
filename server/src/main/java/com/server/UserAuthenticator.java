package com.server;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.*;

public class UserAuthenticator extends BasicAuthenticator {

    private static List<User> users = null;

    public UserAuthenticator() {
        super("warning");
        users = new ArrayList<User>();
        users.add(new User("matti", "salasana"));
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        User findUser = new User(username, password);
        if(users.contains(findUser)) {
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
