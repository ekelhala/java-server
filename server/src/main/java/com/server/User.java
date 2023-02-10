package com.server;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String username;
    private String password;
    private String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Tämän metodin avulla on mahdollista luoda uusi User JSON-objektista.
     * @param userObject JSONObject joka sisältää parametrit username, password ja email
     * @return Uusi User-olio, jonka käyttäjänimi, salasana ja sähköposti ovat userObjectin mukaiset
     * @throws JSONException Jos jokin tarvittu parametri puuttuu tai on virheellinen
     */
    public static User fromJSON(JSONObject userObject) throws JSONException {
        return new User(userObject.getString("username"),
                        userObject.getString("password"), 
                        userObject.getString("email"));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
    
    @Override
    public boolean equals(Object another) {
        if(another instanceof User) {
            User anotherUser = (User) another;
            if(anotherUser.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

}
