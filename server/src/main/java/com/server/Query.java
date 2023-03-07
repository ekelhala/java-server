package com.server;

import java.time.ZonedDateTime;

import org.json.JSONException;
import org.json.JSONObject;

public class Query {
    
    private String type;
    private ZonedDateTime timeStart;
    private ZonedDateTime timeEnd;
    private String user;
    private double upLatitude;
    private double downLatitude;
    private double upLongitude;
    private double downLongitude;

    
    public Query(String timeStart, String timeEnd) {
        type = "time";
        this.timeStart = ZonedDateTime.parse(timeStart);
        this.timeEnd = ZonedDateTime.parse(timeEnd);
    }

    public Query(String user) {
        type = "user";
        this.user = user;
    }

    public Query(double upLatitude, double downLatitude, double upLongitude, double downLongitude) {
        type = "location";
        this.upLatitude = upLatitude;
        this.downLatitude = downLatitude;
        this.upLongitude = upLongitude;
        this.downLongitude = downLongitude;
    }

    public String getType() {
        return type;
    }

    public ZonedDateTime getTimeStart() {
        return timeStart;
    }

    public ZonedDateTime getTimeEnd() {
        return timeEnd;
    }

    public String getUser() {
        return user;
    }

    public double getUpLatitude() {
        return upLatitude;
    }

    public double getDownLatitude() {
        return downLatitude;
    }

    public double getUpLongitude() {
        return upLongitude;
    }

    public double getDownLongitude() {
        return downLongitude;
    }

    public long timeEndMillis() {
        return timeEnd.toInstant().toEpochMilli();
    }

    public long timeStartMillis() {
        return timeStart.toInstant().toEpochMilli();
    }

    public static Query fromJSON(JSONObject withObject) throws JSONException {
        String queryType = withObject.getString("query");
        if(queryType.equals("time")) {
            return new Query(withObject.getString("timestart"), withObject.getString("timeend"));
        }
        else if(queryType.equals("user")) {
            return new Query(withObject.getString("nickname"));
        }
        else if(queryType.equals("location")) {
            return new Query(withObject.getDouble("uplatitude"),
                             withObject.getDouble("downlatitude"),
                             withObject.getDouble("uplongitude"),
                             withObject.getDouble("downlongitude"));
        }
        throw new JSONException("Query type " + queryType + " not supported");
    }
}
