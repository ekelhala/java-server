package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;

import com.server.WarningMessage.DangerType;

public class MessageDB {

    private static MessageDB instance = null;
    private static Connection connection = null;
    private PreparedStatement addNewUserStatement,
                            checkUserStatement,
                            addNewMessageStatement,
                            getAllMessagesStatement,
                            validateUserStatement,
                            queryTimeStatement,
                            queryUserStatement,
                            queryLocationStatement;
    private SecureRandom saltGenerator = new SecureRandom();

    private MessageDB() {
            try {
                validateUserStatement = connection.prepareStatement("select * from users where USERNAME=?");
                addNewUserStatement = connection.prepareStatement("insert into users(USERNAME, PASSWORD, EMAIL) values(?, ?, ?)");
                checkUserStatement = connection.prepareStatement("select 1 from users where USERNAME=?");
                addNewMessageStatement = connection.prepareStatement("insert into messages(NICKNAME, DANGERTYPE, LATITUDE, LONGITUDE, SENT, AREACODE, PHONENUMBER) values(?,?,?,?,?,?,?)");
                getAllMessagesStatement = connection.prepareStatement("select * from messages");
                queryUserStatement = connection.prepareStatement("select * from messages where NICKNAME=?");
                queryTimeStatement = connection.prepareStatement("select * from messages where SENT > ? and SENT < ?");
                queryLocationStatement = connection.prepareStatement("select * from messages where LONGITUDE < ? and LONGITUDE > ? and LATITUDE > ? and LATITUDE < ?");
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
                //generate salt as bytes
                byte[] bytes = new byte[13];
                saltGenerator.nextBytes(bytes);
                //convert salt to string for easier handling and add indicator for SHA-512 
                String saltAsString = new String(Base64.getEncoder().encode(bytes));
                saltAsString = "$6$" + saltAsString;
                //hash the password for storing
                String hashedPassword = Crypt.crypt(userToAdd.getPassword(), saltAsString);
                addNewUserStatement.setString(1, userToAdd.getUsername());
                addNewUserStatement.setString(2, hashedPassword);
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
            ResultSet results = validateUserStatement.executeQuery();
            //If user exists, we go forward, otherwise return false
            if(results.next()) {
                String passwordFromDB = results.getString("PASSWORD");
                if(passwordFromDB.equals(Crypt.crypt(password, passwordFromDB)))
                    return true;
            }
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
            addNewMessageStatement.setString(6, message.getAreaCode());
            addNewMessageStatement.setString(7, message.getPhoneNumber());
            addNewMessageStatement.executeUpdate();
            return true;
        }
        return false;
    }

    public List<WarningMessage> getAllMessages() throws SQLException {
        ResultSet result = getAllMessagesStatement.executeQuery();
        return extractMessages(result);
    }

    public List<WarningMessage> queryMessages(Query query) throws SQLException {
        PreparedStatement queryStatement = null;
        switch(query.getType()) {
            case "user":
                queryStatement = queryUserStatement;
                queryStatement.setString(1,query.getUser());
                break;
            case "time":
                queryStatement = queryTimeStatement;
                queryStatement.setLong(1, query.timeStartMillis());
                queryStatement.setLong(2, query.timeEndMillis());
                break;
            case "location":
                queryStatement = queryLocationStatement;
                queryStatement.setDouble(1, query.getDownLongitude());
                queryStatement.setDouble(2, query.getUpLongitude());
                queryStatement.setDouble(3, query.getDownLatitude());
                queryStatement.setDouble(4, query.getUpLatitude());
                break;
        }
        ResultSet set = queryStatement.executeQuery();
        return extractMessages(set);
    }

    private static void init() throws SQLException {
        if(connection != null) {
            String createUserTable = "create table users(USERNAME VARCHAR(60), PASSWORD VARCHAR(60), EMAIL VARCHAR(60)) ";
            String createMsgTable = "create table messages(NICKNAME VARCHAR(60), DANGERTYPE VARCHAR(10), LONGITUDE DOUBLE, LATITUDE DOUBLE, SENT INTEGER, AREACODE VARCHAR(10), PHONENUMBER VARCHAR(10))";
            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
        }
    }
    
    private List<WarningMessage> extractMessages(ResultSet set) throws SQLException {
        List<WarningMessage> results = new ArrayList<>();
        while(set.next()) {
            String nickname = set.getString("NICKNAME");
            DangerType dangerType = WarningMessage.verifyDangerType(set.getString("DANGERTYPE"));
            double longitude = set.getDouble("LONGITUDE");
            double latitude = set.getDouble("LATITUDE");
            String areaCode = set.getString("AREACODE");
            String phoneNumber = set.getString("PHONENUMBER");
            WarningMessage msg = new WarningMessage(nickname, latitude, longitude, dangerType, areaCode, phoneNumber);
            msg.setSent(LocalDateTime.ofInstant(Instant.ofEpochMilli(set.getLong("SENT")), ZoneOffset.UTC));
            results.add(msg);
        }
        return results;
    }
}
