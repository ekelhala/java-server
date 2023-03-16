package com.server;

import java.time.ZonedDateTime;

import org.json.JSONException;
import org.json.JSONObject;

public class Query {
    
    private QueryType type;
    private ZonedDateTime timeStart;
    private ZonedDateTime timeEnd;
    private String user;
    private double upLatitude;
    private double downLatitude;
    private double upLongitude;
    private double downLongitude;

    public enum QueryType {
        USER("user"),
        TIME("time"),
        LOCATION("location");

        public final String label;

        private QueryType(String label) {
            this.label = label;
        }
    }

    public static class InvalidQueryTypeException extends Exception {

        private String queryType;

        InvalidQueryTypeException(String queryType) {
            super();
            this.queryType = queryType;
        }

        @Override
        public String getMessage() {
            return "Query type '" + queryType + "' not supported.";
        }
    }

    public Query(String timeStart, String timeEnd) {
        type = QueryType.TIME;
        this.timeStart = ZonedDateTime.parse(timeStart);
        this.timeEnd = ZonedDateTime.parse(timeEnd);
    }

    public Query(String user) {
        type = QueryType.USER;
        this.user = user;
    }

    public Query(double upLatitude, double downLatitude, double upLongitude, double downLongitude) {
        type = QueryType.LOCATION;
        this.upLatitude = upLatitude;
        this.downLatitude = downLatitude;
        this.upLongitude = upLongitude;
        this.downLongitude = downLongitude;
    }

    public Query() {}

    public QueryType getType() {
        return type;
    }

    public void setType(QueryType type) {
        this.type = type;
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

    /**
     * Luo uuden Query-objektin JSON-objektista
     * @param withObject JSON-objekti, jossa tarvittavat tiedot uuden objektin luomista varten.
     * @return Query-objekti jossa on JSON-objektin sisältämät tiedot oikeissa muuttujissaan.
     * @throws JSONException Mikäli annetussa JSON-objektissa on vääräntyyppistä tietoa tai kenttiä puuttuu
     * @throws InvalidQueryTypeException Mikäli QueryType ei ole sallittu
     */
    public static Query fromJSON(JSONObject withObject) throws JSONException, InvalidQueryTypeException {
        QueryType queryType = verifyQueryType(withObject.getString("query"));
        switch(queryType) {
            case TIME:
                return new Query(withObject.getString("timestart"), withObject.getString("timeend"));
            case USER:
                return new Query(withObject.getString("nickname"));
            case LOCATION:
                return new Query(withObject.getDouble("uplatitude"),
                withObject.getDouble("downlatitude"),
                withObject.getDouble("uplongitude"),
                withObject.getDouble("downlongitude"));
            default:
                throw new JSONException("JSON not valid");
        }
    }

    /**
     * Tarkistaa annetun tekstin kelvollisuuden QueryTypeksi
     * @param queryType tarkistettava teksti
     * @return saatu QueryType, mikäli teksti oli kelvollinen
     * @throws InvalidQueryTypeException mikäli tekstiä ei ole kelvollinen QueryType
     */
    public static QueryType verifyQueryType(String queryType) throws InvalidQueryTypeException {
        switch(queryType) {
            case "time":
                return QueryType.TIME;
            case "location":
                return QueryType.LOCATION;
            case "user":
                return QueryType.USER;
            default:
                throw new InvalidQueryTypeException(queryType);
        }
    }
}
