package com.hackorama.plethora.server.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.server.web.WebHandler;
import com.hackorama.plethora.server.web.WebServer;
import com.hackorama.plethora.server.web.handler.DynamicPageHandler;
import com.hackorama.plethora.server.web.handler.StaticPageHandler;

public class WebServerTest {

	private static WebServer webServer;
	private static WebHandler webHandler;
	private static StaticPageHandler mockStaticPageHandler;
	private static DynamicPageHandler mockDynamicPageHandler;

	private static String hostName = "localhost";
	private static int port = 9999;

	@BeforeClass
	public static void setUpBeforeClass() {

		// get the collaborators mocked up
		mockDynamicPageHandler = mock(DynamicPageHandler.class);
		mockStaticPageHandler = mock(StaticPageHandler.class);
		webHandler = new WebHandler(mockDynamicPageHandler,
				mockStaticPageHandler);

		// the SUT from collaborators
		webServer = new WebServer(hostName, port, webHandler);
		webServer.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		webServer.stop();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHTTPMetricsServerUp() {
		String name = webServer.getName();
		assertTrue("Web metrics server up test", name != null
				&& name.length() > 0);
	}

	@Test
	public void testHTTPMetricServerStaticRequest() {
		String staticPageRequestPath = "/static/page";
		String staticPageResponse = "static page";
		when(mockStaticPageHandler.handleRequest(staticPageRequestPath))
		.thenReturn(staticPageResponse.getBytes());
		testHttpGet(staticPageRequestPath, staticPageResponse);
	}

	@Test
	public void testHTTPMetricServerDynamicRequest() {
		String mockMetricName = "mockmetric";
		String mockMetricValue = "mockvalue";
		String dynamicMetricRequestPath = "/get/" + mockMetricName;
		String dynamicMetricResponse = mockMetricValue;

		when(mockDynamicPageHandler.handleRequest(dynamicMetricRequestPath))
		.thenReturn(dynamicMetricResponse.getBytes());

		testHttpGet(dynamicMetricRequestPath, dynamicMetricResponse);
	}

	@Test
	public void testHTTPMetricServerMatchingRequests() {
		String mockMetricName = "mockmetric";
		String mockMetricValue = "mockvalue";
		String dynamicMetricRequestPath = "/get/" + mockMetricName;
		String dynamicMetricResponse = mockMetricValue;

		when(mockDynamicPageHandler.handleRequest(dynamicMetricRequestPath))
				.thenReturn(dynamicMetricResponse.getBytes());

		/*
		 * mock up a static page handling with the same request path as the
		 * dynamic metric request. To ensure we don't reach static page handler
		 * It should have been handled by the dynamic handler already
		 */
		String staticPageRequestPath = dynamicMetricRequestPath;
		String staticPageResponse = "should not reach static handler";
		when(mockStaticPageHandler.handleRequest(staticPageRequestPath))
		.thenReturn(staticPageResponse.getBytes());

		testHttpGet(dynamicMetricRequestPath, dynamicMetricResponse);
	}

	private void testHttpGet(String requestPath, String expectedResponse) {
		String requestURL = "http://" + hostName + ":" + port + requestPath;
		String actualResponse = "";
		try {
			actualResponse = getResponse(requestURL);
		} catch (IOException e) {
			fail("IO Exception during HTTP get for " + requestPath);
		}
		System.out.println(requestURL + " : " + expectedResponse + " == "
				+ actualResponse);
		assertEquals("HTTP Metric Server", expectedResponse, actualResponse);
	}

	private String getResponse(String url) throws IOException {
		URL serverUrl = getURL(url);
		if (serverUrl == null) {
			return null; // error logged in getURL
		}
		HttpURLConnection serverConnection = (HttpURLConnection) serverUrl
				.openConnection();
		serverConnection.connect();
		BufferedReader reader = null;
		StringBuilder buffer = new StringBuilder();
		try {
			reader = new BufferedReader(new java.io.InputStreamReader(
					serverConnection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return buffer.toString();
	}

	private URL getURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
		}
		return null;
	}

}
