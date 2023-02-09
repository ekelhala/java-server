package com.server;

public class User {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean equals(Object another) {
        if(another instanceof User) {
            User anotherUser = (User) another;
            if(anotherUser.getUsername().equals(username) && anotherUser.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

}
