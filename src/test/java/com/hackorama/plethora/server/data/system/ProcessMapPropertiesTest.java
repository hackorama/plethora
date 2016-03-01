package com.hackorama.plethora.server.data.system;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.PlethoraTestHelper;
import com.hackorama.plethora.server.data.system.ProcessMapper;
import com.hackorama.plethora.server.data.system.PropertiesProcessMapper;
import com.hackorama.plethora.server.data.system.SigarFacade;
import com.hackorama.plethora.server.data.system.SystemAccess;

public class ProcessMapPropertiesTest {

	private static final String EOL = System.getProperty("line.separator");
	private static String TEST_FILE_CONTENTS = "one = 1111" + EOL
			+ "two = 2222" + EOL
			+ "three = PID_FILE_3333" + EOL
			+ "four = fail" + EOL
			+ "five = PID_FILE_FAIL" + EOL
			+ "six = /this/is/a/bad/file/path" + EOL
			+ "seven = TEST_QUERY_PID_7777" + EOL
			+ "eight = 8888" + EOL
			+ "nine = \"9999\" " + EOL
			+ "ten = \'10\'" + EOL
			+ "eleven =11"  + EOL
			+ "twelve=12"  + EOL
			+ "thirteen=    13  ";
	private static ProcessMapper processMapTest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SystemAccess mockSysAccess = mock(SigarFacade.class);
		when(mockSysAccess.findPid("TEST_QUERY_PID_7777")).thenReturn(
				new Long(7777));
		when(mockSysAccess.isAvailable()).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(1111)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(2222)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(3333)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(7777)).thenReturn(true);

		when(mockSysAccess.isAvaialbleProcess(8888)).thenReturn(false);

		when(mockSysAccess.isAvaialbleProcess(9999)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(10)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(11)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(12)).thenReturn(true);
		when(mockSysAccess.isAvaialbleProcess(13)).thenReturn(true);

		processMapTest = new PropertiesProcessMapper(
				setUpTestConfigFile(), mockSysAccess);
	}

	private static String setUpTestConfigFile() {
		// create an valid pid file and insert the path into config file content
		TEST_FILE_CONTENTS = TEST_FILE_CONTENTS.replace("PID_FILE_3333",
				PlethoraTestHelper.portableFilePath(PlethoraTestHelper
						.genTestFile("3333")));
		// create an invalid pid file and insert the path into config file
		TEST_FILE_CONTENTS = TEST_FILE_CONTENTS.replace("PID_FILE_FAIL",
				PlethoraTestHelper.portableFilePath(PlethoraTestHelper
						.genTestFile("fail")));
		// create the config file and return the path
		return PlethoraTestHelper.genTestFile(TEST_FILE_CONTENTS);
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
	public void testValidValues() {
		System.out.println(processMapTest.getMap());
		assertEquals(new Long(1111), processMapTest.getMap().get("one"));
		assertEquals(new Long(2222), processMapTest.getMap().get("two"));
		assertEquals(new Long(3333), processMapTest.getMap().get("three"));

	}

	@Test
	public void testInvalidValues() {
		assertEquals(new Long(0), processMapTest.getMap().get("four"));
		assertEquals(new Long(0), processMapTest.getMap().get("five"));
		assertEquals(new Long(0), processMapTest.getMap().get("six"));
	}

	@Test
	public void testValidPidNotRunning() {
		assertEquals(new Long(0), processMapTest.getMap().get("eight"));
	}

	@Test
	public void testPropValueFormats() {
		assertEquals(new Long(9999), processMapTest.getMap().get("nine"));
		assertEquals(new Long(10), processMapTest.getMap().get("ten"));
		assertEquals(new Long(11), processMapTest.getMap().get("eleven"));
		assertEquals(new Long(12), processMapTest.getMap().get("twelve"));
		assertEquals(new Long(13), processMapTest.getMap().get("thirteen"));
	}

	@Test
	public void testPidQuery() {
		assertEquals(new Long(7777), processMapTest.getMap().get("seven"));
	}

	@Test
	public void testAccessMethods() {
		assertEquals("Test constructor and method based access",
				processMapTest.getMap(),
				processMapTest.getMap(setUpTestConfigFile()));

	}
}
