package com.server;

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
    private String latitude;
    private String longitude;
    private DangerType dangerType;
    
    public WarningMessage(String nickname, String latitude, String longitude, DangerType dangerType) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerType = dangerType;
    }

    public String getNickname() {
        return nickname;
    }

    public String[] getCoordinates() {
        return new String[]{latitude, longitude};
    }

    public DangerType getDangerType() {
        return dangerType;
    }

    /**
     * Luo WarningMessagen JSON-objektista
     * @param object JSONObjekti, joka sisältää tarvitut tiedot
     * @return Uusi WarningMessage jossa on JSONObjectin määrittelemät tiedot
     * @throws JSONException Jos object ei sisällä kaikkia tarvittuja kenttiä
     */
    public static WarningMessage fromJSON(JSONObject object) throws JSONException {
        return new WarningMessage(object.getString("nickname"), object.getString("latitude"),
                                    object.getString("longitude"), 
                                    verifyDangerType(object.getString("dangertype")));
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
        return result;
    }

    //Apumetodi, jolla varmistetaan että DangerType on oikeanlainen
    private static DangerType verifyDangerType(String typeString) {
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
                throw(new JSONException("Field dangertype is invalid"));
        }
    }
}
