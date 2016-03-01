package com.hackorama.plethora.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.PlethoraTestHelper;
import com.hackorama.plethora.config.ProtectedConfiguration;

public class ProtectedConfigurationTest {

	private static final String EOL = System.getProperty("line.separator");
	private static final String TEST_FILE_CONTENTS = "# comment " + EOL +
			" foo = bar " + EOL +
			"# comment 2" + EOL +
			" foo1 = bar " + EOL +
			"   foo1 = bar   " + EOL +
			"   foo1=bar   " + EOL +
			" foo1  bar " + EOL +
			"   foo1 bar   " + EOL +
			"   foo1:bar   " + EOL +
			"   foo1:bar   " + EOL +
			"   foo1 :bar   " + EOL +
			"   foo1 : bar   " + EOL +
			" foo1 = bar1" + EOL +
			" foo 1 = bar 1" + EOL +
			" foo1 = bar 1" + EOL +
			"# comment 2" + EOL +
			"" + EOL +
			" foo2 =  bar2 "+ EOL +
			" " + EOL +
			"# comment3" + EOL +
			" foo = bar3 " + EOL +
			" " + EOL +
			" #" + EOL +
			"# comment4" + EOL +
			"#" + EOL +
			"" + EOL +
			" foo4 = bar ";

	private static ProtectedConfiguration protectedConfigurtion;
	private static HashMap<String, String> originalValues;
	private static HashMap<String, String> updatedValues;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String filePath = PlethoraTestHelper.genTestFile(TEST_FILE_CONTENTS);
		assertNotNull("Test property file is set up", filePath);
		protectedConfigurtion = new ProtectedConfiguration(filePath);
		originalValues = new HashMap<String, String>();
		updatedValues = new HashMap<String, String>();
		originalValues.put("foo1", "bar");
		updatedValues.put("foo1", "PROTECTEDBAR");
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
	public void propertyRegexFilterTest() {
		assertTrue("foo1=PROTECTEDBAR".equals(testFilter("foo1=bar")));
		assertTrue(" foo1=PROTECTEDBAR".equals(testFilter(" foo1=bar")));
		assertTrue("  foo1=PROTECTEDBAR".equals(testFilter("  foo1=bar")));
		assertTrue("foo1=PROTECTEDBAR ".equals(testFilter("foo1=bar ")));
		assertTrue("foo1=PROTECTEDBAR  ".equals(testFilter("foo1=bar  ")));
		assertTrue(" foo1=PROTECTEDBAR ".equals(testFilter(" foo1=bar ")));
		assertTrue("  foo1=PROTECTEDBAR  ".equals(testFilter("  foo1=bar  ")));
		assertTrue("  foo1 =PROTECTEDBAR  ".equals(testFilter("  foo1 =bar  ")));
		assertTrue("  foo1= PROTECTEDBAR  ".equals(testFilter("  foo1= bar  ")));
		assertTrue("  foo1=  PROTECTEDBAR  "
				.equals(testFilter("  foo1=  bar  ")));
		assertTrue("  foo1  =PROTECTEDBAR  "
				.equals(testFilter("  foo1  =bar  ")));
		assertTrue("  foo1 = PROTECTEDBAR  "
				.equals(testFilter("  foo1 = bar  ")));
		assertTrue("  foo1  =  PROTECTEDBAR  "
				.equals(testFilter("  foo1  =  bar  ")));

		assertTrue("foo1:PROTECTEDBAR".equals(testFilter("foo1:bar")));
		assertTrue(" foo1:PROTECTEDBAR".equals(testFilter(" foo1:bar")));
		assertTrue("  foo1:PROTECTEDBAR".equals(testFilter("  foo1:bar")));
		assertTrue("foo1:PROTECTEDBAR ".equals(testFilter("foo1:bar ")));
		assertTrue("foo1:PROTECTEDBAR  ".equals(testFilter("foo1:bar  ")));
		assertTrue(" foo1:PROTECTEDBAR ".equals(testFilter(" foo1:bar ")));
		assertTrue("  foo1:PROTECTEDBAR  ".equals(testFilter("  foo1:bar  ")));
		assertTrue("  foo1 :PROTECTEDBAR  ".equals(testFilter("  foo1 :bar  ")));
		assertTrue("  foo1: PROTECTEDBAR  ".equals(testFilter("  foo1: bar  ")));
		assertTrue("  foo1:  PROTECTEDBAR  "
				.equals(testFilter("  foo1:  bar  ")));
		assertTrue("  foo1  :PROTECTEDBAR  "
				.equals(testFilter("  foo1  :bar  ")));
		assertTrue("  foo1 : PROTECTEDBAR  "
				.equals(testFilter("  foo1 : bar  ")));
		assertTrue("  foo1  :  PROTECTEDBAR  "
				.equals(testFilter("  foo1  :  bar  ")));

		assertTrue(" foo1   PROTECTEDBAR".equals(testFilter(" foo1   bar")));
		assertTrue("foo1 PROTECTEDBAR".equals(testFilter("foo1 bar")));
		assertTrue(" foo1 PROTECTEDBAR".equals(testFilter(" foo1 bar")));
		assertTrue("  foo1 PROTECTEDBAR".equals(testFilter("  foo1 bar")));
		assertTrue("foo1 PROTECTEDBAR ".equals(testFilter("foo1 bar ")));
		assertTrue("foo1 PROTECTEDBAR  ".equals(testFilter("foo1 bar  ")));
		assertTrue(" foo1 PROTECTEDBAR ".equals(testFilter(" foo1 bar ")));
		assertTrue("  foo1  PROTECTEDBAR  ".equals(testFilter("  foo1  bar  ")));

		assertTrue("foo1=\"PROTECTEDBAR\"".equals(testFilter("foo1=\"bar\"")));
		assertTrue("foo1= \"PROTECTEDBAR\"".equals(testFilter("foo1= \"bar\"")));
		assertTrue("foo1=\"PROTECTEDBAR\" ".equals(testFilter("foo1=\"bar\" ")));
		assertTrue("foo1= \"PROTECTEDBAR\" "
				.equals(testFilter("foo1= \"bar\" ")));
		assertTrue("foo1=  \"PROTECTEDBAR\""
				.equals(testFilter("foo1=  \"bar\"")));
		assertTrue("foo1=\"PROTECTEDBAR\"  "
				.equals(testFilter("foo1=\"bar\"  ")));
		assertTrue("foo1=  \"PROTECTEDBAR\"  "
				.equals(testFilter("foo1=  \"bar\"  ")));
		assertTrue("foo1= \"PROTECTEDBAR\"  "
				.equals(testFilter("foo1= \"bar\"  ")));
		assertTrue("foo1=  \"PROTECTEDBAR\" "
				.equals(testFilter("foo1=  \"bar\" ")));

		assertTrue("foo1:\"PROTECTEDBAR\"".equals(testFilter("foo1:\"bar\"")));
		assertTrue("foo1 \"PROTECTEDBAR\"".equals(testFilter("foo1 \"bar\"")));
		assertTrue("foo1: \"PROTECTEDBAR\"".equals(testFilter("foo1: \"bar\"")));
		assertTrue("foo1  \"PROTECTEDBAR\"".equals(testFilter("foo1  \"bar\"")));
		assertTrue("foo1: \"PROTECTEDBAR\" "
				.equals(testFilter("foo1: \"bar\" ")));
		assertTrue("foo1  \"PROTECTEDBAR\"  "
				.equals(testFilter("foo1  \"bar\"  ")));

		assertFalse("foo1=\"PROTECTEDBAR\"".equals(testFilter("foo1=\"bar")));
		assertFalse("foo1=\"PROTECTEDBAR\"".equals(testFilter("foo1=bar\"")));

		assertTrue("foo1=\"PROTECTEDBAR".equals(testFilter("foo1=\"bar")));
		assertTrue("foo1=PROTECTEDBAR\"".equals(testFilter("foo1=bar\"")));

		assertTrue(" koo : baz".equals(testFilter(" koo : baz")));
	}

	private String testFilter(String line) {
		Object[] params = new Object[3];
		params[0] = line;
		params[1] = originalValues;
		params[2] = updatedValues;
		try {
			return (String) PlethoraTestHelper.privateAccess(
					protectedConfigurtion,
					"filterLine", params);
		} catch (Exception e) {
			e.printStackTrace();
			fail("FilterLine failed");
		}
		return null;
	}

	@Test
	public void propertyReplaceTest() {
		Object[] params = new Object[2];
		params[0] = originalValues;
		params[1] = updatedValues;
		String result = null;
		try {
			result = (String) PlethoraTestHelper.privateAccess(
					protectedConfigurtion,
					"getUpdatedProperties", params);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed updating properties test");
		}
		System.out.println(result);
		assertNotNull(result);
	}

}
