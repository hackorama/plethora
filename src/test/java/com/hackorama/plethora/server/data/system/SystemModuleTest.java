package com.hackorama.plethora.server.data.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.common.data.Metrics;
import com.hackorama.plethora.config.SystemConfiguration;
import com.hackorama.plethora.server.data.system.ProcessMapper;
import com.hackorama.plethora.server.data.system.SYSTEM_METRIC_TYPE;
import com.hackorama.plethora.server.data.system.SigarFacade;
import com.hackorama.plethora.server.data.system.SystemAccess;
import com.hackorama.plethora.server.data.system.SystemAccessException;
import com.hackorama.plethora.server.data.system.SystemModule;

public class SystemModuleTest {

	private static SystemModule testSysModule;
	private static SystemModule testSysModuleNotAvail;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConcurrentHashMap<String, Long> mockMap = new ConcurrentHashMap<String, Long>();
		mockMap.put("one", (long) 1111);
		mockMap.put("two", (long) 0);
		mockMap.put("three", (long) 3333);
		mockMap.put("four", (long) 4444);

		Metrics mockMetrics = Metrics.getInstance();
		SystemConfiguration mockSysConf = mock(SystemConfiguration.class);
		ProcessMapper mockProcMap = mock(ProcessMapper.class);
		SystemAccess mockSysAccess = mock(SigarFacade.class);
		SystemAccess mockSysAccessNotAvail = mock(SigarFacade.class);
		when(mockSysAccessNotAvail.getName()).thenReturn("MockBadModule");

		when(mockProcMap.getMap()).thenReturn(mockMap);
		when(mockSysAccess.isAvailable()).thenReturn(true);
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.cpu, 1111)).thenReturn(
				(long) 1);
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.cpu, 3333)).thenReturn(
				(long) 3);
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.cpu, 4444)).thenThrow(
				new SystemAccessException("Failed"));
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.cpu)).thenReturn(
				(long) 10);

		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.memory, 1111))
		.thenReturn((long) 11);
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.memory, 3333))
		.thenReturn((long) 33);
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.memory, 4444))
		.thenThrow(
				new SystemAccessException("Failed"));
		when(mockSysAccess.getMetric(SYSTEM_METRIC_TYPE.memory)).thenReturn(
				(long) 20);

		testSysModule = new SystemModule(mockMetrics, mockSysConf,
				mockSysAccess, mockProcMap);
		testSysModuleNotAvail = new SystemModule(mockMetrics, mockSysConf,
				mockSysAccessNotAvail, mockProcMap);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSysAccessNotAvailable() {
		assertNull(testSysModuleNotAvail.getValue("fail.cpu"));
	}

	@Test
	public void testCpu() {
		assertEquals(new Long(10), testSysModule.getValue("cpu"));
	}

	@Test
	public void testCpuPid() {
		assertEquals(new Long(1), testSysModule.getValue("one.cpu"));
		assertEquals(new Long(3), testSysModule.getValue("three.cpu"));
	}

	@Test
	public void testCpuPidNotAvailable() {
		assertNull(testSysModule.getValue("two.cpu"));
	}

	@Test
	public void testCpuPidException() {
		assertNull(testSysModule.getValue("four.cpu"));
	}

	@Test
	public void testMem() {
		assertEquals(new Long(20), testSysModule.getValue("memory"));
	}

	@Test
	public void testMemPid() {
		assertEquals(new Long(11), testSysModule.getValue("one.memory"));
		assertEquals(new Long(33), testSysModule.getValue("three.memory"));
	}

	@Test
	public void testMemPidNotAvailable() {
		assertNull(testSysModule.getValue("two.memory"));
	}

	@Test
	public void testMemPidException() {
		assertNull(testSysModule.getValue("four.memory"));
	}

	@Test
	public void testInvalidMetricNames() {

		assertNull(testSysModule.getValue("one.unknown"));
		assertNull(testSysModule.getValue("unknown.memory"));
		assertNull(testSysModule.getValue("unknown.unknown"));
		assertNull(testSysModule.getValue("unknown"));
		assertNull(testSysModule.getValue(".unknown"));
		assertNull(testSysModule.getValue("cpu.cpu"));
		assertNull(testSysModule.getValue(".cpu"));
		assertNull(testSysModule.getValue("cpu."));
		assertNull(testSysModule.getValue("."));
		assertNull(testSysModule.getValue(""));
		assertNull(testSysModule.getValue(" "));
		assertNull(testSysModule.getValue(null));
		assertNull(testSysModule.getValue("unknown.cpu"));
		assertNull(testSysModule.getValue("cpu.unknown"));
	}
}
