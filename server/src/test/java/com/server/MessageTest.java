package com.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.*;
import org.junit.*;

import com.server.WarningMessage.DangerType;
import com.server.WarningMessage.InvalidDangerTypeException;

public class MessageTest {

    WarningMessage instance;

    //Esimerkkiarvot
    private String nickname = "Example";
    private double latitude = 10;
    private double longitude = 10;
    private DangerType dangerType = DangerType.DEER;
    private String areaCode = "123";
    private String phoneNumber = "1234566789";
    private ZonedDateTime sent = ZonedDateTime.now();
    private int id = 0;

    @Before
    public void setupTest() {
        instance = new WarningMessage(nickname, latitude, longitude, dangerType, areaCode, phoneNumber);
        instance.setSent(sent);
        instance.setId(id);
    }

    @Test(expected = InvalidDangerTypeException.class)
    public void testInvalidDangerType() throws InvalidDangerTypeException {
        WarningMessage.verifyDangerType("invalid");
    }

    @Test(expected = JSONException.class)
    public void testInvalidJSON() throws InvalidDangerTypeException {
        WarningMessage.fromJSON(new JSONObject());
    }

    @Test
    public void testToJSON() throws JSONException {
        //Muutetaan esimerkkiobjekti JSON-muotoon
        JSONObject result = instance.toJSON();
        //Luodaan vertailuobjekti ja laitetaan siihen manuaalisesti esimerkkiarvot
        JSONObject compare = new JSONObject();
        compare.put("nickname", nickname);
        compare.put("latitude", latitude);
        compare.put("longitude", longitude);
        compare.put("dangertype", dangerType.label);
        compare.put("areacode", areaCode);
        compare.put("phonenumber", phoneNumber);
        compare.put("sent", sent.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));
        compare.put("id", id);
        //Saatujen objektien tulee olla samat
        assertTrue(compare.similar(result));
    }

    /*
     * Testataan että kaikki arvot asettuvat objektiin oikein.
     */
    @Test
    public void testValues() {
        assertEquals(nickname, instance.getNickname());
        assertEquals(latitude, instance.getCoordinates()[0], 0);
        assertEquals(longitude, instance.getCoordinates()[1],0);
        assertEquals(dangerType, instance.getDangerType());
        assertEquals(areaCode, instance.getAreaCode());
        assertEquals(phoneNumber, instance.getPhoneNumber());
        assertEquals(sent.toInstant().toEpochMilli(), instance.sentAsMillis());
        assertEquals(id, instance.getId());
    }

}
