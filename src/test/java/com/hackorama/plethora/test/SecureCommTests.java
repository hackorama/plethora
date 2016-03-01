package com.hackorama.plethora.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/*
 * 
 *  openssl pkcs12 -in spin-developer.p12 -out tmp.pem
 *  openssl pkcs12 -export -in tmp.pem -out plethora.p12
 *  rm tmp.pem
 *
 */
public class SecureCommTests {

	private SSLSocketFactory getFactory(String keyFileName, String keyPassword, String keyType) throws Exception {
		if (keyType == null) {
			keyType = KeyStore.getDefaultType();
		}
		File keyFile = new File(keyFileName);
		KeyStore keyStore = initKeyStore(keyFile, keyPassword, keyType);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(initKeys(keyStore, keyPassword, "SunX509"), initTrust(), new SecureRandom());
		setHandling(context);

		return context.getSocketFactory();
	}

	private static KeyStore initKeyStore(File keyFile, String keyPassword, String keyType) throws KeyStoreException,
	NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = KeyStore.getInstance(keyType);
		InputStream keyInput = null;
		try {
			keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyPassword.toCharArray());
		} finally {
			if (keyInput != null) {
				keyInput.close();
			}
		}
		return keyStore;
	}

	private KeyManager[] initKeys(KeyStore keyStore, String keyPassword, String algorithm)
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
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		return trusty;
	}

	private void setHandling(SSLContext sslContext) {
		SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

	}

	private void setVerifier(HttpsURLConnection httpsConnection) {
		httpsConnection.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;// TODO add any restrictions here
			}
		});
	}

	private String remoteRequest(String url, String certpath, String password) throws IOException {
		if (url == null) {
			return null;
		}
		URL serverUrl;
		try {
			serverUrl = new URL(url);
		} catch (MalformedURLException e) {
			System.out.println("Failed because of invalid URL " + url);
			return null;
		}

		HttpsURLConnection serverConnection = (HttpsURLConnection) serverUrl.openConnection();
		serverConnection.setReadTimeout(0);
		serverConnection.setConnectTimeout(0);

		try {
			serverConnection.setSSLSocketFactory(getFactory(certpath, password, "PKCS12"));
			setVerifier(serverConnection);
		} catch (UnrecoverableKeyException e) {
			System.out.println("Failed unrecoverable key during SSL setup for " + url);
			return null;
		} catch (KeyManagementException e) {
			System.out.println("Failed  key management during SSL setup for " + url);
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Failed no such algo during SSL setup for " + url);
			return null;
		} catch (KeyStoreException e) {
			System.out.println("Failed keystore during SSL setup for " + url);
			return null;
		} catch (CertificateException e) {
			System.out.println("Failed cert during SSL setup for " + url);
			return null;
		} catch (Exception e) {
			System.out.println("Failed during SSL setup for " + url);
			e.printStackTrace();
			return null;
		}

		System.out.println("URL : " + url);

		serverConnection.connect();
		// printCerts(serverConnection);

		String line = null;
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(serverConnection.getInputStream()));
		while ((line = br.readLine()) != null) {
			sb.append(line + '\n');
		}
		System.out.println(sb.toString());
		return sb.toString();

	}

	private byte[] getPayload(String token, String facade, String method, String args) {
		return new String(token + "\n" + facade + "\n" + method + "\n" + args).getBytes(); // token facade method args
	}

	private void doPost(HttpsURLConnection conn, byte[] payload) {
		InputStream in;
		OutputStream out = null;
		int http_status;

		try {
			out = conn.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			out.write(payload);
			http_status = conn.getResponseCode();
			System.out.println("Response code " + http_status);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			in = conn.getInputStream();
			readResponse(in);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			conn.disconnect();
		}
	}

	private static byte[] readStream(InputStream in) throws IOException {
		byte[] buf = new byte[1024];
		int count = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		while ((count = in.read(buf)) != -1) {
			out.write(buf, 0, count);
		}
		return out.toByteArray();
	}

	private void readResponse(InputStream in) {
		System.out.println(in.toString());
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			byte[] body = readStream(bin);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printCerts(HttpsURLConnection connection) {
		if (connection == null) {
			return;
		}
		try {
			System.out.println("Response Code : " + connection.getResponseCode());
			System.out.println("Cipher Suite : " + connection.getCipherSuite());
			System.out.println("\n");

			Certificate[] certs = connection.getServerCertificates();
			for (Certificate cert : certs) {
				System.out.println("Cert Type : " + cert.getType());
				System.out.println("Cert Hash Code : " + cert.hashCode());
				System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
				System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
				System.out.println("\n");
			}
		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length > 2) {
			new SecureCommTests().remoteRequest(args[0], args[1], args[2]);
		} else {
			new SecureCommTests().remoteRequest("https://192.168.136.192:1003", 
					"tests/resources/tests/plethora.p12", "opsware_admin");
		}
	}

}
