package com.hackorama.plethora.server.data.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

public final class Crypto {

    private Crypto() {

    }

    public static SSLSocketFactory getFactory(String keyFileName, String keyPassword, String keyType)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            KeyManagementException, UnrecoverableKeyException {
        String theKeyType = keyType;
        if (theKeyType == null) {
            theKeyType = KeyStore.getDefaultType();
        }
        File keyFile = new File(keyFileName);
        KeyStore keyStore = initKeyStore(keyFile, keyPassword, theKeyType);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(initKeys(keyStore, keyPassword, "SunX509"), initTrust(), new SecureRandom());
        setHandling(context);

        return context.getSocketFactory();
    }

    public static void setVerifier(HttpsURLConnection httpsConnection) {
        httpsConnection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private static KeyStore initKeyStore(File keyFile, String keyPassword, String keyType) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyType);
        InputStream keyInput = null;
        try {
            keyInput = new FileInputStream(keyFile);
            keyStore.load(keyInput, keyPassword.toCharArray());
        } finally {
            Util.close(keyInput, Log.getLogger());
        }
        return keyStore;
    }

    private static KeyManager[] initKeys(KeyStore keyStore, String keyPassword, String algorithm)
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        keyManagerFactory.init(keyStore, keyPassword.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] initTrust() {
        // X509 trust manager that does not validate certificate chains
        TrustManager[] trusty = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // allow without checking
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // allow without checking
            }
        } };
        return trusty;
    }

    private static TrustManager[] alternateInitTrust(KeyStore keyStore, String algorithm)
            throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static void setHandling(SSLContext sslContext) {
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

    }

    public static void printCerts(HttpsURLConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            String data = "Response Code : " + connection.getResponseCode() + " Cipher Suite : "
                    + connection.getCipherSuite();
            Log.getLogger().info(data);
            Certificate[] certs = connection.getServerCertificates();
            for (Certificate cert : certs) {
                data = cert.getType() + ", " + cert.hashCode() + ", " + cert.getPublicKey().getAlgorithm() + ", "
                        + cert.getPublicKey().getFormat();
                Log.getLogger().info("Cert Info : " + data);
            }
        } catch (SSLPeerUnverifiedException e) {
            Log.getLogger().log(Level.SEVERE, "Unverified peer", e);
        } catch (IOException e) {
            Log.getLogger().log(Level.SEVERE, "IO error", e);
        }
    }

    public static void printContent(HttpsURLConnection connection) {
        if (connection != null) {
            Log.getLogger().info("Content : " + connection.getURL());
        }
    }

    public static void printHeaders(HttpsURLConnection connection) {
        if (connection == null) {
            return;
        }
        StringBuilder data = new StringBuilder().append("Headers : ");
        for (int i = 0; i < connection.getHeaderFields().size(); i++) {
            String name = connection.getHeaderFieldKey(i);
            String value = connection.getHeaderField(i);
            if (name != null && value != null) {
                data.append(" ").append(name).append("=").append(value);
            } else if (value != null) {
                data.append(" Response code = ").append(value);
            }
        }
        Log.getLogger().info(data.toString());
    }

}
