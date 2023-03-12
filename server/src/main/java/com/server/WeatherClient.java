package com.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
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

    private static String serverAddress = "https://localhost:4001/weather";
    private static URL requestURL;
    private static String cert,certPass;

    public static void initialize(String cert, String certPass) throws MalformedURLException {
        requestURL = new URL(serverAddress);
        WeatherClient.cert = cert;
        WeatherClient.certPass = certPass;
    }

    public static String getTemperature(double[] coordinates) {
        Document requestBody = buildRequestBody(coordinates[0], coordinates[1]);
        String requestContent = buildRequestContent(requestBody);
        try {
            byte[] requestBytes = requestContent.getBytes("UTF-8");
            HttpsURLConnection requestConnection = prepareRequest(requestBytes.length);
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

    private static String getResponse(HttpsURLConnection connection, byte[] requestContent) {
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

    private static HttpsURLConnection prepareRequest(int contentLength) {
        try {
            /*
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(cert), certPass.toCharArray());
            Certificate certificate = ks.getCertificate("alias");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("localhost", certificate);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);*/
            HttpsURLConnection request = (HttpsURLConnection) requestURL.openConnection();
            request.setSSLSocketFactory(initializeCertSSLContext().getSocketFactory());
            request.setRequestMethod("POST");
            request.setRequestProperty("Content-Type", "application/xml");
            request.setRequestProperty("Content-Length", String.valueOf(contentLength));
            request.setDoInput(true);
            request.setDoOutput(true);
            return request;
        }
        catch(Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static SSLContext initializeCertSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(cert), certPass.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, certPass.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;
        }
        catch(Exception exception) {
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
