package com.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.json.JSONException;
import org.json.JSONObject;

public class WarningMessage {

    public enum DangerType {
        MOOSE("Moose"),
        REINDEER("Reindeer"),
        DEER("Deer"),
        OTHER("Other");

        public final String label;

        private DangerType(String label) {
            this.label = label;
        }
    }

    private String nickname;
    private double latitude;
    private double longitude;
    private DangerType dangerType;
    private LocalDateTime sent;
    
    public WarningMessage(String nickname, double latitude, double longitude, DangerType dangerType, String sent) throws DateTimeParseException {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerType = dangerType;
        this.sent = LocalDateTime.ofInstant(Instant.parse(sent), ZoneId.of("UTC"));
    }

    public WarningMessage(String nickname, double latitude, double longitude, DangerType dangerType) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerType = dangerType;
    }

    public String getNickname() {
        return nickname;
    }

    public double[] getCoordinates() {
        return new double[]{latitude, longitude};
    }

    public DangerType getDangerType() {
        return dangerType;
    }

    public void setSent(LocalDateTime sent) {
        this.sent = sent;
    }

    /**
     * Luo WarningMessagen JSON-objektista
     * @param object JSONObjekti, joka sisältää tarvitut tiedot
     * @return Uusi WarningMessage jossa on JSONObjectin määrittelemät tiedot
     * @throws JSONException Jos object ei sisällä kaikkia tarvittuja kenttiä
     */
    public static WarningMessage fromJSON(JSONObject object) throws JSONException, DateTimeParseException {
        return new WarningMessage(object.getString("nickname"), object.getDouble("latitude"),
                                    object.getDouble("longitude"), 
                                    verifyDangerType(object.getString("dangertype")),
                                    object.getString("sent"));
    }

    /**
     * Luo JSONObject-esityksen tästä WarningMessagesta
     * @return JSONObject-jossa on kaikki tämän WarningMessagen tiedot oikeissa kentissään
     */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        ZonedDateTime sentZoned = ZonedDateTime.of(sent, ZoneId.of("UTC"));
        result.put("nickname", nickname);
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("dangertype", dangerType.label);
        result.put("sent", sentZoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));
        return result;
    }

    public long sentAsMillis() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    //Apumetodi, jolla varmistetaan että DangerType on oikeanlainen
    public static DangerType verifyDangerType(String typeString) {
        switch(typeString) {
            case "Moose":
                return DangerType.MOOSE;
            case "Deer":
                return DangerType.DEER;
            case "Reindeer":
                return DangerType.REINDEER;
            case "Other":
                return DangerType.OTHER;
            default:
                return null;
        }
    }
}
