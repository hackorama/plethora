package com.hackorama.plethora.server.web.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.web.handler.DATA_TYPE;
import com.hackorama.plethora.server.web.handler.DynamicPageHandler;
import com.hackorama.plethora.server.web.handler.Formatters;

public class DynamicPageHandlerTest {

	// the system under test (SUT)
	private static DynamicPageHandler dynamicPageHandler;

	// the mocked collaborators
	private static MetricService mockMetricService;
	private static Formatters mockFormatters;
	private static Set<DATA_TYPE> mockDataTypes;

	@BeforeClass
	public static void beforeAllTests() {
		// set up the mock collaborators
		mockMetricService = mock(MetricService.class);
		when(mockMetricService.getMetric("one")).thenReturn(1);

		mockDataTypes = new HashSet<DATA_TYPE>();
		mockDataTypes.add(DATA_TYPE.JSON);

		mockFormatters = mock(Formatters.class);
		when(mockFormatters.getKnownDataTypes()).thenReturn(mockDataTypes);

		// create the SUT from mocked collaborators
		dynamicPageHandler = new DynamicPageHandler(mockMetricService,
				mockFormatters);

	}

	@AfterClass
	public static void afterAllTests() {

	}

	@Before
	public void beforeEachTest() {
		// Verify the mocked collaborators are setup and available
		assertEquals("Testing mock MetricService", 1,
				mockMetricService.getMetric("one"));
		assertEquals("Testing mock Formatters", true, mockFormatters
				.getKnownDataTypes().contains(DATA_TYPE.JSON));
		assertEquals("Testing mock Formatters", false, mockFormatters
				.getKnownDataTypes().contains(DATA_TYPE.SITESCOPE));
	}

	@After
	public void afterEachTest() {

	}

	@Test
	public void testHandleRequestValidInputs() {
		// Expected positive result values
		testHandleRequest("/get/one", "1");
		testHandleRequest("/json/get/one", "1");
		// the request path is case sensitive
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html Sec. 3.2.3
		testHandleRequest("/JSON/GET/ONE");
		testHandleRequest("/JsOn/GeT/oNe");
	}

	@Test
	public void testHandleRequestDefaultHandlng() {
		// Expected default value "null" as a string (not null value)
		testHandleRequest("/get/fail", "null");
		testHandleRequest("/get", "null");
	}

	@Test
	public void testHandleRequestInvalidInputs() {
		// Expected null value for invalid inputs
		testHandleRequest("/fail");
		testHandleRequest("/fail/get/one");
		testHandleRequest("/fail/get");
		testHandleRequest("/fail/fail");
		testHandleRequest("/fail/fail/fail");
	}

	private void testHandleRequest(String request, String expectedResult) {
		// cast the byte[] returned to string before checking
		assertEquals("Testing " + request, expectedResult, new String(
				dynamicPageHandler.handleRequest(request)));
	}

	private void testHandleRequest(String request) {
		// for null result no string conversion
		assertNull("Testing " + request,
				dynamicPageHandler.handleRequest(request));
	}

}
