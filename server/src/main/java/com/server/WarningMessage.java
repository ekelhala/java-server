package com.server;

public class WarningMessage {

    public enum DangerType {
        MOOSE,
        REINDEER,
        DEER,
        OTHER
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

}
