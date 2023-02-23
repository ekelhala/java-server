package com.server;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.server.WarningMessage.DangerType;

public class MessageDB {

    private static MessageDB instance = null;
    private static Connection connection = null;
    private static PreparedStatement addNewUserStatement,
                            checkUserStatement,
                            addNewMessageStatement,
                            getAllMessagesStatement;
    private static PreparedStatement validateUserStatement;

    private MessageDB() {
            try {
                validateUserStatement = connection.prepareStatement("select 1 from users where USERNAME=? and PASSWORD=?");
                addNewUserStatement = connection.prepareStatement("insert into users(USERNAME, PASSWORD, EMAIL) values(?, ?, ?)");
                checkUserStatement = connection.prepareStatement("select 1 from users where USERNAME=?");
                addNewMessageStatement = connection.prepareStatement("insert into messages(NICKNAME, DANGERTYPE, LATITUDE, LONGITUDE, SENT) values(?,?,?,?,?)");
                getAllMessagesStatement = connection.prepareStatement("select * from messages");
            }
            catch(SQLException e) {
                e.printStackTrace();
            }
    }

    public static synchronized MessageDB getInstance() {
        if(instance == null) {
            return new MessageDB();
        }
        return instance;
    }

    public static void open(String path) throws SQLException {
        boolean exists = new File(path).isFile();
        connection = DriverManager.getConnection("jdbc:sqlite:"+path);
        if(!exists) {
            init();
        }
    }

    public boolean addNewUser(User userToAdd) throws SQLException {
        if(!doesUserExist(userToAdd)) {
            if(connection != null) {
                addNewUserStatement.setString(1, userToAdd.getUsername());
                addNewUserStatement.setString(2, userToAdd.getPassword());
                addNewUserStatement.setString(3, userToAdd.getEmail());
                addNewUserStatement.executeUpdate();
                return true;
            }
        }
        return false;
    }

    private boolean doesUserExist(User checkUser) throws SQLException {
        if(connection != null) {
            checkUserStatement.setString(1, checkUser.getUsername());
            if(checkUserStatement.executeQuery().next()) {
                return true;
            }
        }
        return false;
    }

    public boolean validateUser(String username, String password) throws SQLException {
        if(connection != null) {
            validateUserStatement.setString(1, username);
            validateUserStatement.setString(2, password);
            ResultSet results = validateUserStatement.executeQuery();
            if(results.next()) {
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean addNewMessage(WarningMessage message) throws SQLException {
        if(connection != null) {
            addNewMessageStatement.setString(1, message.getNickname());
            addNewMessageStatement.setString(2, message.getDangerType().label);
            addNewMessageStatement.setDouble(3, message.getCoordinates()[0]);
            addNewMessageStatement.setDouble(4, message.getCoordinates()[1]);
            addNewMessageStatement.setLong(5, message.sentAsMillis());
            addNewMessageStatement.executeUpdate();
            return true;
        }
        return false;
    }

    public List<WarningMessage> getAllMessages() throws SQLException {
        List<WarningMessage> results = new ArrayList<>();
        ResultSet messageSet = getAllMessagesStatement.executeQuery();
        while(messageSet.next()) {
            String nickname = messageSet.getString("NICKNAME");
            DangerType dangerType = WarningMessage.verifyDangerType(messageSet.getString("DANGERTYPE"));
            double longitude = messageSet.getDouble("LONGITUDE");
            double latitude = messageSet.getDouble("LATITUDE");
            WarningMessage msg = new WarningMessage(nickname, latitude, longitude, dangerType);
            msg.setSent(LocalDateTime.ofInstant(Instant.ofEpochMilli(messageSet.getLong("SENT")), ZoneOffset.UTC));
            results.add(msg);
        }
        return results;
    }

    private static void init() throws SQLException {
        if(connection != null) {
            String createUserTable = "create table users(USERNAME VARCHAR(60), PASSWORD VARCHAR(60), EMAIL VARCHAR(60)) ";
            String createMsgTable = "create table messages(NICKNAME VARCHAR(60), DANGERTYPE VARCHAR(10), LONGITUDE DOUBLE, LATITUDE DOUBLE, SENT INTEGER)";
            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
        }
    }
    
}
