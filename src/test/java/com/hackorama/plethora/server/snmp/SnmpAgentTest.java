package com.hackorama.plethora.server.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.hackorama.plethora.config.MetaConfiguration;
import com.hackorama.plethora.config.SnmpConfiguration;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.io.SecureFileWriter;
import com.hackorama.plethora.server.snmp.SnmpAgent;

public class SnmpAgentTest {

	private static SnmpAgent snmpAgent;

	private static String hostName = "localhost";
	private static int port = 9997;

	private static String oidNumbers = "1.3.6.1.4.1.11.999.99";
	private static String plethoraOid = oidNumbers + ".1";
	private static String oidNames = "iso.org.dod.internet.private.enterprises.hackorama.software.cloudy";

	// MIB RFC 1213 iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0
	private final String sysDescrOid = "1.3.6.1.2.1.1.1.0";
	private static final String mockSysDescr = "Mock sys descr";

	private static String mockModule = "mockmodule";
	private static String mockMetricOneText = "mockmetric";
	private static int mockMetricTwoNumber = 42;
	private static boolean mockMetricThreeBool = true;
	private static int mockMetricThreeBoolExpected = 1; // true == 1 in SNMP
	private static boolean mockMetricFourBool = false;
	private static int mockMetricFourBoolExpected = 0; // false == 0 in SNMP

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		MetricService mockMetricService = mock(MetricService.class);
		when(mockMetricService.getModule(0)).thenReturn(mockModule);
		when(mockMetricService.getMetric(0, 0)).thenReturn(mockMetricOneText);
		when(mockMetricService.getMetric(0, 1)).thenReturn(mockMetricTwoNumber);
		when(mockMetricService.getMetric(0, 2)).thenReturn(mockMetricThreeBool);
		when(mockMetricService.getMetric(0, 3)).thenReturn(mockMetricFourBool);

		MetaConfiguration mockMetaConfiguration = mock(MetaConfiguration.class);
		when(mockMetaConfiguration.getProductName()).thenReturn("plethoratest");

		SnmpConfiguration mockSnmpConfiguration = mock(SnmpConfiguration.class);
		when(mockSnmpConfiguration.getSnmpHostname()).thenReturn(hostName);
		when(mockSnmpConfiguration.getSnmpPort()).thenReturn(port);
		when(mockSnmpConfiguration.getSnmpSysDescr()).thenReturn(mockSysDescr);
		when(mockSnmpConfiguration.getSnmpSysContact()).thenReturn(
				"Mock sys scontact");
		when(mockSnmpConfiguration.getSnmpSysLocation()).thenReturn(
				"Mock sys location");
		when(mockSnmpConfiguration.getSnmpEnterpriseSubTreeNumbers())
		.thenReturn(oidNumbers);
		when(mockSnmpConfiguration.getSnmpEnterpriseSubTreeNames()).thenReturn(
				oidNames);
		when(mockSnmpConfiguration.getMetaConfiguration()).thenReturn(
				mockMetaConfiguration);

		SecureFileWriter mockSecureFileWriter = mock(SecureFileWriter.class);


		snmpAgent = new SnmpAgent(mockSnmpConfiguration, mockMetricService,
				mockSecureFileWriter);
		snmpAgent.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		snmpAgent.stop();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSnmpAgentUp() {
		String name = snmpAgent.getName();
		assertTrue("SNMP agent server up test", name != null
				&& name.length() > 0);
	}

	@Test
	public void testAgentGetModule() {
		testSnmpGet(plethoraOid + ".1", mockModule);
	}

	@Test
	public void testAgentGetMetrics() {
		testSnmpGet(plethoraOid + ".1.1", mockMetricOneText);
		testSnmpGet(plethoraOid + ".1.2", mockMetricTwoNumber);
		testSnmpGet(plethoraOid + ".1.3", mockMetricThreeBoolExpected);
		testSnmpGet(plethoraOid + ".1.4", mockMetricFourBoolExpected);
	}

	@Test
	public void testAgentGetNegetive() {
		testSnmpGet(plethoraOid + ".2");
		testSnmpGet(plethoraOid + ".1.5");
		testSnmpGet(plethoraOid + ".2.1");
	}

	@Test
	public void testAgentGetSysDescr() {
		testSnmpGet(sysDescrOid, mockSysDescr);
	}

	private void testSnmpGet(String testOid, Object expectedValue) {
		try {
			assertEquals("SNMP metric get test",
					testOid + " = " + expectedValue,
					snmpGet(hostName, port, testOid));
		} catch (IOException e) {
			fail("IO Exception during SNMP get for " + testOid);
		}
	}

	private void testSnmpGet(String testOid) {
		testSnmpGet(testOid, "noSuchObject");
	}

	private String snmpGet(String hostname, int port, String testOid)
			throws IOException {
		// Create TransportMapping and Listen
		TransportMapping<?> transport = new DefaultUdpTransportMapping();
		transport.listen();

		// Create Target Address object
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString("public"));
		comtarget.setVersion(SnmpConstants.version1);
		comtarget.setAddress(new UdpAddress(hostname + "/" + port));
		comtarget.setRetries(2);
		comtarget.setTimeout(1000);

		// Create the PDU object
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(testOid)));
		pdu.setType(PDU.GET);
		pdu.setRequestID(new Integer32(1));

		// Create SNMP object for sending data to Agent
		Snmp snmp = new Snmp(transport);

		ResponseEvent response = snmp.get(pdu, comtarget);

		// Process Agent Response
		if (response != null) {
			PDU responsePDU = response.getResponse();

			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					if (responsePDU.getVariableBindings() != null
							&& responsePDU.getVariableBindings().size() > 0) {
						System.out.println("SNMP Response : "
								+ responsePDU.getVariableBindings()
								.get(0).toString());
						return responsePDU.getVariableBindings().get(0)
								.toString();
					} else {
						System.out.println("Error: Empty or null response ");
					}
				} else {
					System.out.println("Error: Request Failed");
					System.out.println("Error Status = " + errorStatus);
					System.out.println("Error Index = " + errorIndex);
					System.out.println("Error Status = " + errorStatusText);
				}
			} else {
				System.out.println("Error: Response PDU is null");
			}
		} else {
			System.out.println("Error: Agent Timeout... ");
		}
		snmp.close();
		return null;
	}

}
