package com.hackorama.plethora.server.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.common.data.Metric;
import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.common.jmx.PlethoraMBean;
import com.hackorama.plethora.server.jmx.ServerJMXAgent;

public class ServerJMXAgentTest {

	private static ServerJMXAgent serverJMXAgent;
	private static PlethoraMBean plethoraMBean;
	private static Metrics metrics;
	private static MBeanServerConnection mBeanServerConnection;

	private static String moduleName = "MockJMXModule";
	private static String hostName = "localhost";
	private static int port = 9998;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		metrics = Metrics.getInstance();
		initMockMetrics();
		plethoraMBean = new PlethoraMBean(metrics);
		serverJMXAgent = new ServerJMXAgent(moduleName, plethoraMBean,
				hostName, port);
		serverJMXAgent.start();
		mBeanServerConnection = null; //connect before test setUp
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		serverJMXAgent.stop();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJMXServerUP() {
		String name = serverJMXAgent.getName();
		assertTrue("JMX metrics server up test", name != null
				&& name.length() > 0);
	}

	@Test
	public void testJMXServerGetMetricValues() {
		makeConnection(hostName, port);
		verifyAttribute("metric1", (long) 1);
		verifyAttribute("metric2", "two");
		verifyAttribute("metric3", true);
	}

	@Test
	public void testJMXServerGetInvalidMetricValues() {
		verifyAttribute("invalid");
	}

	private static void initMockMetrics() {
		metrics.addMetric(new Metric<Long>("metric1", (long) 1));
		metrics.addMetric(new Metric<String>("metric2", "two"));
		metrics.addMetric(new Metric<Boolean>("metric3", true));
	}

	private static void makeConnection(String host, int port) {
		try {
			if( mBeanServerConnection == null ){
				mBeanServerConnection = JMXConnectorFactory.connect(
						new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host
								+ ":" + port + "/jmxrmi"),
								new HashMap<String, Object>())
								.getMBeanServerConnection();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL connecting to jmx server " + host + ":" + port);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO error connecting to jmx server " + host + ":" + port);
		}
	}

	private void verifyAttribute(String attributeName, Object expectedValue) {
		assertEquals("Getting metric from JMX server", expectedValue,
				getAttribute(attributeName));
	}

	private void verifyAttribute(String attributeName) {
		assertNull("Getting metric from JMX server",
				getAttribute(attributeName));
	}
	private Object getAttribute(String attribute) {
		makeConnection(hostName, port);
		ObjectName objectName = null;
		try {
			// Match com.hackorama.plethora.common.jmxJMXResolver.moduleJMXName()
			objectName = new ObjectName("plethora:name=" + moduleName);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
			fail("Error in objectname for getting attribute " + attribute);
		} catch (NullPointerException e) {
			e.printStackTrace();
			fail("Error in objectname for getting attribute " + attribute);
		}
		try {
			return mBeanServerConnection.getAttribute(objectName, attribute);
		} catch (AttributeNotFoundException e) {
			// e.printStackTrace();
			// fail("Error getting attribute " + attribute);
		} catch (InstanceNotFoundException e) {
			// e.printStackTrace();
			// fail("Error getting attribute " + attribute);
		} catch (MBeanException e) {
			// e.printStackTrace();
			// fail("Error getting attribute " + attribute);
		} catch (ReflectionException e) {
			// e.printStackTrace();
			// fail("Error getting attribute " + attribute);
		} catch (IOException e) {
			// e.printStackTrace();
			// fail("Error getting attribute " + attribute);
		}
		return null;
	}

	private static void promptUser(String message) {
		/* if we need to wait for any interactive tests */
		try {
			System.out.println("\n" + message);
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
