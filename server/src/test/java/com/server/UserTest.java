package com.server;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.*;

public class UserTest {

    private String username = "Example";
    private String password = "Password123";
    private String email = "email@email.com";
    private User instance;

    @Before
    public void setupTest() {
        instance = new User(username, password, email);
    }

    @Test
    public void testFromJSON() {
        JSONObject object = new JSONObject();
        object.put("username", username);
        object.put("password", password);
        object.put("email", email);
        User created = User.fromJSON(object);
        assertEquals(created, instance);
    }

    @Test
    public void testGetEmail() {
        assertEquals(email, instance.getEmail());
    }

    @Test
    public void testGetPassword() {
        assertEquals(password, instance.getPassword());
    }

    @Test
    public void testGetUsername() {
        assertEquals(username, instance.getUsername());
    }
}
