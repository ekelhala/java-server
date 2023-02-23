package com.server;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.HttpsParameters;

public class Server {

    private static HttpsServer server;
    public static void main( String[] args )
    {
        try {
            server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext ctx = getServerSSLContext(args[0], args[1]);
            server.setHttpsConfigurator(new HttpsConfigurator(ctx) {
                public void configure(HttpsParameters parameters) {
                    SSLContext context = getSSLContext();
                    SSLParameters sslParameters = context.getDefaultSSLParameters();
                    //parameters.setSSLParameters(sslParameters);
                }
            });
            MessageDB.open(args[2]);
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }
        HttpContext warningContext = server.createContext("/warning", new MessageHandler());
        UserAuthenticator auth = new UserAuthenticator();
        server.createContext("/registration", new RegistrationHandler(auth));
        warningContext.setAuthenticator(auth);
        server.setExecutor(null);
        server.start();
    }

    private static SSLContext getServerSSLContext(String keyStoreName, String password) throws Exception {
        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStoreName), passphrase);
     
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
     
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
     
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }
}
