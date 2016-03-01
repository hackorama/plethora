package com.hackorama.plethora.server.data.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.config.CryptoConfiguration;

public final class Connector {

    private static final String WEB_PROTOCOL = "https://";
    private final String name;
    private final String host;
    private final int port;
    private final URLBuilder urlBuilder;
    private final CryptoConfiguration configuration;

    public Connector(String name, String host, int port, String path, CryptoConfiguration configuration) {
        if (!Util.validPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        this.name = name;
        this.host = host;
        this.port = port;
        this.configuration = configuration;
        urlBuilder = new URLBuilder(buildURL(path));

    }

    public String buildURL(String path) {
        StringBuilder result = new StringBuilder();
        result.append(WEB_PROTOCOL);
        result.append(host);
        result.append(":");
        result.append(port);
        if (path != null) {
            result.append("/");
            result.append(path);
        }
        return result.toString();
    }

    public String getResponse(String url) throws IOException {
        URL serverUrl = getURL(url);
        if (serverUrl == null) {
            return null; // error logged in getURL
        }
        HttpsURLConnection serverConnection = secureConnection((HttpsURLConnection) serverUrl.openConnection());
        if (serverConnection == null) {
            throw new IOException("Could not get a valid secure connection");
        }
        setConnectionOptions(serverConnection);
        serverConnection.connect();
        // debugConnection(serverConnection);
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();
        try {
            reader = new BufferedReader(new java.io.InputStreamReader(serverConnection.getInputStream(),
                    Util.getEncoding()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + System.getProperty("line.separator"));
            }
        } finally {
            Util.close(reader, Log.getLogger());
        }
        return buffer.toString();
    }

    public URLBuilder getUrlBuilder() {
        return urlBuilder;
    }

    private HttpsURLConnection secureConnection(HttpsURLConnection connection) throws IOException {
        if (connection == null) {
            return null;
        }
        try {
            connection.setSSLSocketFactory(Crypto.getFactory(configuration.getKeyFileName(),
                    configuration.getKeyPassword(), configuration.getKeyType()));
            Crypto.setVerifier(connection);
            return connection;
        } catch (UnrecoverableKeyException e) {
            secureConnectionException(connection.getURL(), e);
        } catch (KeyManagementException e) {
            secureConnectionException(connection.getURL(), e);
        } catch (NoSuchAlgorithmException e) {
            secureConnectionException(connection.getURL(), e);
        } catch (KeyStoreException e) {
            secureConnectionException(connection.getURL(), e);
        } catch (CertificateException e) {
            secureConnectionException(connection.getURL(), e);
        } catch (Exception e) {
            secureConnectionException(connection.getURL(), e);
        }
        throw new IOException("Could not secure the connection");
    }

    private URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.getLogger().warning(
                    "Connection failed for web module " + name + " because of invalid URL " + url + ", "
                            + e.getMessage());
        }
        return null;
    }

    private void secureConnectionException(URL url, Exception exception) {
        // print full stack trace only at FINE log level
        String msg = "Secure connection failed for web module " + name + " at " + url;
        Log.getLogger().warning(msg + ", " + exception.getMessage());
        Log.getLogger().log(Level.FINE, msg, exception);
    }

    private void debugConnection(HttpsURLConnection connection) {
        if (connection != null) {
            Crypto.printCerts(connection);
            Crypto.printHeaders(connection);
            Crypto.printContent(connection);
        }
    }

    private void setConnectionOptions(HttpsURLConnection connection) {
        // Please set any addition properties to the connection here like :
        // System.setProperty("http.keepAlive", "false");
        if (connection != null) {
            // connection.setReadTimeout(0);
            // connection.setConnectTimeout(0);
        }
    }
}
