package com.server;

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
    private ZonedDateTime sent;
    private String areaCode;
    private String phoneNumber;
    private String byUser;
    private ZonedDateTime modified;
    private String updateReason;
    private int id;
    private String temperature = "";
    
    public WarningMessage(String nickname, double latitude,
                        double longitude, DangerType dangerType, 
                        String sent) throws DateTimeParseException {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerType = dangerType;
        this.sent = ZonedDateTime.parse(sent, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    public WarningMessage(String nickname, double latitude, double longitude, DangerType dangerType,
                            String areaCode, String phoneNumber) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerType = dangerType;
        this.areaCode = areaCode;
        this.phoneNumber = phoneNumber;
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

    public void setSent(ZonedDateTime sent) {
        this.sent = sent;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getByUser() {
        return byUser;
    }

    public void setByUser(String byUser) {
        this.byUser = byUser;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public String getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     * Luo WarningMessagen JSON-objektista
     * @param object JSONObjekti, joka sisältää tarvitut tiedot
     * @return Uusi WarningMessage jossa on JSONObjectin määrittelemät tiedot
     * @throws JSONException Jos object ei sisällä kaikkia tarvittuja kenttiä
     */
    public static WarningMessage fromJSON(JSONObject object) throws JSONException, DateTimeParseException {
        WarningMessage result = new WarningMessage(object.getString("nickname"), object.getDouble("latitude"),
                                    object.getDouble("longitude"), 
                                    verifyDangerType(object.getString("dangertype")),
                                    object.getString("sent"));
        handlePhoneContact(object, result);
        setIdIfExists(object, result);
        return result;
    }

    private static void handlePhoneContact(JSONObject object, WarningMessage message) {
        if(object.has("areacode"))
            message.setAreaCode(object.getString("areacode"));
        else
            message.setAreaCode("nodata");
        if(object.has("phonenumber"))
            message.setPhoneNumber(object.getString("phonenumber"));
        else 
            message.setPhoneNumber("nodata");
    }

    private static void setIdIfExists(JSONObject object, WarningMessage message) {
        if(object.has("id")) {
            message.setId(object.getInt("id"));
            message.setUpdateReason(object.getString("updatereason"));
            message.setModified(ZonedDateTime.now());
        }
    }

    /**
     * Luo JSONObject-esityksen tästä WarningMessagesta
     * @return JSONObject-jossa on kaikki tämän WarningMessagen tiedot oikeissa kentissään
     */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("nickname", nickname);
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("dangertype", dangerType.label);
        result.put("sent", sent.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));
        result.put("id", id);
        if(!areaCode.equals("nodata"))
            result.put("areacode", areaCode);
        if(!phoneNumber.equals("nodata"))
            result.put("phonenumber", phoneNumber);
        if(modified != null) {
            result.put("modified", modified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));
            result.put("updatereason", updateReason);
        }
        if(!temperature.equals(""))
            result.put("weather", temperature);
        return result;
    }

    public long sentAsMillis() {
        return sent.toInstant().toEpochMilli();
    }

    public long modifiedAsMillis() {
        return modified.toInstant().toEpochMilli();
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
