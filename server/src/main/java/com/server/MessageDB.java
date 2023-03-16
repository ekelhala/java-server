package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;

import com.server.WarningMessage.DangerType;
import com.server.WarningMessage.InvalidDangerTypeException;

/**
 * Tämä luokka mahdollistaa tietokantaan liittyvien luku- ja kirjoitusoperaatioiden suorittamisen.
 * Luokka on toteutettu Singleton-periaatetta käyttäen, eli metodeja voidaan käyttää aluksi hankkimalla
 * viittaus getInstance()-metodilla ja sen jälkeen kutsumalla muita luokan metodeja.
 * Luokka on alustettava kutsumalla open()-metodia ennen kuin sitä voidaan käyttää tietokantaoperaatioiden suorittamiseen.
 */
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
                            queryLocationStatement,
                            updateMessageStatement,
                            checkMessageStatement;
    private SecureRandom saltGenerator = new SecureRandom();

    private MessageDB() {
            try {
                validateUserStatement = connection.prepareStatement("select * from users where USERNAME=?");
                addNewUserStatement = connection.prepareStatement("insert into users(USERNAME, PASSWORD, EMAIL) values(?, ?, ?)");
                checkUserStatement = connection.prepareStatement("select 1 from users where USERNAME=?");
                addNewMessageStatement = connection.prepareStatement("insert into messages(NICKNAME, DANGERTYPE, LATITUDE, LONGITUDE, SENT, AREACODE, PHONENUMBER, BYUSER, MODIFIED, UPDATEREASON, TEMPERATURE) values(?,?,?,?,?,?,?,?,?,?,?)");
                getAllMessagesStatement = connection.prepareStatement("select * from messages");
                queryUserStatement = connection.prepareStatement("select * from messages where NICKNAME=?");
                queryTimeStatement = connection.prepareStatement("select * from messages where SENT > ? and SENT < ?");
                queryLocationStatement = connection.prepareStatement("select * from messages where LONGITUDE < ? and LONGITUDE > ? and LATITUDE > ? and LATITUDE < ?");
                checkMessageStatement = connection.prepareStatement("select 1 from messages where ID=? and BYUSER=?");
                updateMessageStatement = connection.prepareStatement("update messages set NICKNAME=?, DANGERTYPE=?, LATITUDE=?, LONGITUDE=?, AREACODE=?, PHONENUMBER=?, MODIFIED=?, UPDATEREASON=? where ID=?");
            }
            catch(SQLException e) {
                e.printStackTrace();
            }
    }

    /**
     * Palauttaa viittauksen tähän luokkaan.
     * @return MessageDB-objekti joka mahdollistaa tietokannan käyttämisen.
     */
    public static synchronized MessageDB getInstance() {
        if(instance == null) {
            return new MessageDB();
        }
        return instance;
    }

    /**
     * Avaa yhteyden tietokantaan ja alustaa tämän luokan.
     * 
     * @param path osoite tietokantaan, johon yhteys avataan.
     * @throws SQLException Mikäli yhteyden avaaminen epäonnistuu.
     */
    public static void open(String path) throws SQLException {
        boolean exists = new File(path).isFile();
        connection = DriverManager.getConnection("jdbc:sqlite:"+path);
        if(!exists) {
            init();
        }
    }

    /**
     * Lisää käyttäjän uuden tietokantaan
     * @param userToAdd Käyttäjä, joka halutaan lisätä.
     * @return false, mikäli käyttäjä oli jo tietokannassa ja true mikäli ei.
     * @throws SQLException Mikäli tapahtuu virhe tietokantaa käsitellessä.
     */
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

    /**
     * Tarkistaa, onko parametrina annettu käyttäjä jo tietokannassa.
     * @param checkUser käyttäjä, joka halutaan tarkistaa
     * @return false jos käyttäjä ei ole tietokannassa ja true jos on.
     * @throws SQLException jos tietokantaa käsitellessä tapahtui virhe.
     */
    private boolean doesUserExist(User checkUser) throws SQLException {
        if(connection != null) {
            checkUserStatement.setString(1, checkUser.getUsername());
            if(checkUserStatement.executeQuery().next()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tarkistaa, onko annettu käyttäjä tietokannassa ja onko käyttäjälle annettu salasana oikea.
     * @param username tarkistettavan käyttäjän käyttäjänimi
     * @param password tarkistettavan käyttäjän salasana
     * @return true, mikäli käyttäjä on tietokannassa ja parametrina annettu salasana on oikea, false jos ei.
     * @throws SQLException mikäli tietokantaa käsitellessä tapahtui virhe.
     */
    public boolean validateUser(String username, String password) throws SQLException {
        if(connection != null) {
            validateUserStatement.setString(1, username);
            ResultSet results = validateUserStatement.executeQuery();
            //Jos käyttäjä on olemassa, mennään eteenpäin, muuten palautetaan false
            if(results.next()) {
                String passwordFromDB = results.getString("PASSWORD");
                if(passwordFromDB.equals(Crypt.crypt(password, passwordFromDB)))
                    return true;
            }
        }
        return false;
    }

    /**
     * Lisää uuden viestin tietokantaan.
     * @param message viesti, joka halutaan tietokantaan lisätä.
     * @return true, yhteys tietokantaan oli auki ja viesti voitiin lisätä, false jos ei.
     * @throws SQLException mikäli tietokantaa käsitellessä tapahtui virhe.
     */
    public boolean addNewMessage(WarningMessage message) throws SQLException {
        if(connection != null) {
            addNewMessageStatement.setString(1, message.getNickname());
            addNewMessageStatement.setString(2, message.getDangerType().label);
            addNewMessageStatement.setDouble(3, message.getCoordinates()[0]);
            addNewMessageStatement.setDouble(4, message.getCoordinates()[1]);
            addNewMessageStatement.setLong(5, message.sentAsMillis());
            addNewMessageStatement.setString(6, message.getAreaCode());
            addNewMessageStatement.setString(7, message.getPhoneNumber());
            addNewMessageStatement.setString(8, message.getByUser());
            addNewMessageStatement.setLong(9, 0);
            addNewMessageStatement.setString(10, "");
            addNewMessageStatement.setString(11, message.getTemperature());
            addNewMessageStatement.executeUpdate();
            return true;
        }
        return false;
    }

    /**
     * Palauttaa listan, joka sisältää kaikki tietokantaan tallennetut viestit.
     * @return Lista WarningMessage-objekteja jotka kuvaavat kaikkia tietokannan viestejä
     * @throws SQLException Mikäli tietokantaa käsitellessä tapahtui virhe
     * @throws InvalidDangerTypeException Mikäli viestejä luodessa havaittiin vääräntyyppinen DangerType
     */
    public List<WarningMessage> getAllMessages() throws SQLException, InvalidDangerTypeException {
        ResultSet result = getAllMessagesStatement.executeQuery();
        return extractMessages(result);
    }

    /**
     * Mahdollistaa tietyntyyppisten viestien hakemisen tietokannasta.
     * @param query Kuvailee haluttujen viestin ominaisuudet
     * @return Lista, joka sisältää halutut ehdot täyttävät viestit
     * @throws SQLException Mikäli tietokantaa käsitellessä tapahtui virhe
     * @throws InvalidDangerTypeException Mikäli viestejä luodessa havaittiin vääräntyyppinen DangerType
     */
    public List<WarningMessage> queryMessages(Query query) throws SQLException, InvalidDangerTypeException {
        PreparedStatement queryStatement = null;
        switch(query.getType()) {
            case USER:
                queryStatement = queryUserStatement;
                queryStatement.setString(1,query.getUser());
                break;
            case TIME:
                queryStatement = queryTimeStatement;
                queryStatement.setLong(1, query.timeStartMillis());
                queryStatement.setLong(2, query.timeEndMillis());
                break;
            case LOCATION:
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

    /**
     * Mahdollistaa tietokantaan tallennettujen viestien tallentamisen. Mikäli haluttua viestiä ei löytynyt, tallentaa uuden viestin
     * tietokantaan.
     * @param message kuvailee halutun viestin.
     * @throws SQLException mikäli tietokantaa käsitellessä tapahtui virhe.
     */
    public void editOrCreate(WarningMessage message) throws SQLException {
        checkMessageStatement.setInt(1, message.getId());
        checkMessageStatement.setString(2, message.getByUser());
        ResultSet set = checkMessageStatement.executeQuery();
        if(set.next()) {
            updateMessageStatement.setString(1, message.getNickname());
            updateMessageStatement.setString(2, message.getDangerType().label);
            updateMessageStatement.setDouble(3, message.getCoordinates()[0]);
            updateMessageStatement.setDouble(4, message.getCoordinates()[1]);
            updateMessageStatement.setString(5, message.getAreaCode());
            updateMessageStatement.setString(6, message.getPhoneNumber());
            updateMessageStatement.setLong(7, message.modifiedAsMillis());
            updateMessageStatement.setString(8, message.getUpdateReason());
            updateMessageStatement.setInt(9, message.getId());
            updateMessageStatement.executeUpdate();
        }
        else {
            addNewMessage(message);
        }
    }

    //Luo tietokannan
    private static void init() throws SQLException {
        if(connection != null) {
            String createUserTable = "create table users(USERNAME VARCHAR(60), PASSWORD VARCHAR(60), EMAIL VARCHAR(60)) ";
            String createMsgTable = "create table messages(ID INTEGER PRIMARY KEY, NICKNAME VARCHAR(60), DANGERTYPE VARCHAR(10), LONGITUDE DOUBLE, LATITUDE DOUBLE, SENT INTEGER, AREACODE VARCHAR(10), PHONENUMBER VARCHAR(10), BYUSER VARCHAR(60), MODIFIED INTEGER, UPDATEREASON VARCHAR(60), TEMPERATURE VARCHAR(10))";
            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
        }
    }
    
    //Apufunktio WarningMessage-listan luomiseen ResultSetistä.
    private List<WarningMessage> extractMessages(ResultSet set) throws SQLException, InvalidDangerTypeException {
        List<WarningMessage> results = new ArrayList<>();
        while(set.next()) {
            String nickname = set.getString("NICKNAME");
            DangerType dangerType = WarningMessage.verifyDangerType(set.getString("DANGERTYPE"));
            double longitude = set.getDouble("LONGITUDE");
            double latitude = set.getDouble("LATITUDE");
            String areaCode = set.getString("AREACODE");
            String phoneNumber = set.getString("PHONENUMBER");
            long modified = set.getLong("MODIFIED");
            int id = set.getInt("ID");
            String temperature = set.getString("TEMPERATURE");
            WarningMessage msg = new WarningMessage(nickname, latitude, longitude, dangerType, areaCode, phoneNumber);
            msg.setSent(ZonedDateTime.ofInstant(Instant.ofEpochMilli(set.getLong("SENT")), ZoneOffset.UTC));
            if(modified != 0) {
                String updateReason = set.getString("UPDATEREASON");
                msg.setModified(ZonedDateTime.ofInstant(Instant.ofEpochMilli(modified), ZoneOffset.UTC));
                msg.setUpdateReason(updateReason);
            }
            msg.setId(id);
            msg.setTemperature(temperature);
            results.add(msg);
        }
        return results;
    }
}
