package com.hackorama.plethora.i18n;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.PlethoraTestHelper;
import com.hackorama.plethora.common.Configuration;

public class Encoding {
	private static final String EOL = System.getProperty("line.separator");
	private static final String TEST_FILE_CONTENTS = "# test encoding " + EOL +
			"english = hello" + EOL +
			"unicode = \u0068\u0065\u006C\u006c\u006F" + EOL
			+ "kanji = \u4e30" + EOL;

	static Configuration config;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String filePath = PlethoraTestHelper.genTestFile(TEST_FILE_CONTENTS);
		config = new Configuration(filePath);
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
	public void testEncoding() {
		System.out.println(config.getProperty("english"));
		System.out.println(config.getProperty("unicode"));
		System.out.println(config.getProperty("kanji"));
	}

}
