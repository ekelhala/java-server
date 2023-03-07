package com.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class WeatherClient {

    private static String serverAddress = "http://localhost:4001/weather";
    private static URL requestURL;

    public static void initialize() throws MalformedURLException {
        requestURL = new URL(serverAddress);
    }

    public static String getTemperature(double[] coordinates) {
        Document requestBody = buildRequestBody(coordinates[0], coordinates[1]);
        String requestContent = buildRequestContent(requestBody);
        try {
            byte[] requestBytes = requestContent.getBytes("UTF-8");
            HttpURLConnection requestConnection = prepareRequest(requestBytes.length);
            String response = getResponse(requestConnection, requestBytes);
            Document responseDocument = parseResponse(response);
            Element rootElement = responseDocument.getDocumentElement();
            NodeList list = rootElement.getElementsByTagName("temperature");
            return list.item(0).getTextContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Document parseResponse(String response) {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            return dBuilder.parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getResponse(HttpURLConnection connection, byte[] requestContent) {
        try {
            OutputStream contentOutput = connection.getOutputStream();
            contentOutput.write(requestContent);
            contentOutput.flush();
            if(connection.getResponseCode() == 200) {
                InputStream responseStream = connection.getInputStream();
                String line = "";
                StringBuffer responseBody = new StringBuffer();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
                while((line = responseReader.readLine()) != null) {
                    responseBody.append(line);
                }
                return responseBody.toString();
            }
            return null;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static HttpURLConnection prepareRequest(int contentLength) {
        try {
            HttpURLConnection request = (HttpURLConnection)requestURL.openConnection();
            request.setRequestMethod("POST");
            request.setRequestProperty("Content-Type", "application/xml");
            request.setRequestProperty("Content-Length", String.valueOf(contentLength));
            request.setDoInput(true);
            request.setDoOutput(true);
            return request;
        }
        catch(IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static String buildRequestContent(Document requestBody) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(requestBody), new StreamResult(writer));
            return writer.toString();            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Document buildRequestBody(double latitude, double longitude) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            Document document = builder.newDocument();
            //Luo dokumentti
            Element coordinatesElement = document.createElement("coordinates");
            document.appendChild(coordinatesElement);
            //Latitude
            Element latitudeElement = document.createElement("latitude");
            latitudeElement.appendChild(document.createTextNode(Double.toString(latitude)));
            coordinatesElement.appendChild(latitudeElement);
            //Longitude
            Element longitudeElement = document.createElement("longitude");
            longitudeElement.appendChild(document.createTextNode(Double.toString(longitude)));
            coordinatesElement.appendChild(longitudeElement);
            return document;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    } 
}
