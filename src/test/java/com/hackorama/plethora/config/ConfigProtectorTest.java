package com.hackorama.plethora.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hackorama.plethora.PlethoraTestHelper;
import com.hackorama.plethora.config.ConfigProtector;
import com.hackorama.plethora.config.Protector;

public class ConfigProtectorTest {

	static Protector protector;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		protector = new ConfigProtector();
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
	public void testChecksum() {
		URL configProtectorClass = ConfigProtector.class
				.getResource("ConfigProtector.class");
		File file = new File(configProtectorClass.getPath());
		String checkSumOne = null;
		String checkSumTwo = null;
		try {
			Object[] params = { file };
			checkSumOne = (String) PlethoraTestHelper.privateAccess(protector,
					"checksum", params);
			checkSumTwo = (String) PlethoraTestHelper.privateAccess(protector,
					"checksum", params);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Checksum failed");
		}
		System.out.println(checkSumOne + " = " + checkSumTwo);
		assertEquals("Check repeatable checksum", checkSumOne, checkSumTwo);
	}

	@Test
	public void testProtects() {
		String testValue = "opsware_admin";
		String protectedValue = null;
		try {
			Object[] params = { testValue };
			protectedValue = (String) PlethoraTestHelper.privateAccess(
					protector, "protect",
					params);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Protect failed");
		}
		String unProtectedValue = null;
		try {
			Object[] params = { protectedValue };
			unProtectedValue = (String) PlethoraTestHelper.privateAccess(
					protector, "unProtect",
					params);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unprotect failed");
		}
		System.out.println(testValue + " = [" + protectedValue + "] = "
				+ unProtectedValue);
		assertEquals("Check protect unprotect", testValue, unProtectedValue);
		assertFalse(testValue.equals(protectedValue));
	}

	@Test
	public void testSeals() throws IOException, SecurityException,
	IllegalArgumentException, NoSuchMethodException,
	IllegalAccessException, InvocationTargetException {
		String testValue = "test";
		Object[] params = { testValue };
		String sealedValue = (String) PlethoraTestHelper.privateAccess(
				protector, "seal", params);
		params[0] = sealedValue;
		String unSealedValue = (String) PlethoraTestHelper.privateAccess(
				protector, "unSeal",
				params);
		System.out.println(testValue + " = [" + sealedValue + "] = "
				+ unSealedValue);
		assertEquals("Check seal unseal", testValue, unSealedValue);
		assertFalse(testValue.equals(sealedValue));

	}

	@Test
	public void testTags() throws SecurityException, IllegalArgumentException,
	NoSuchMethodException, IllegalAccessException,
	InvocationTargetException {
		String testValue = "opsware_admin";
		Object[] params = new Object[1];
		params[0] = testValue;
		String taggedValue = (String) PlethoraTestHelper.privateAccess(
				protector, "tag", params);
		params[0] = taggedValue;
		String unTaggedValue = (String) PlethoraTestHelper.privateAccess(
				protector, "unTag",
				params);
		System.out.println(testValue + " = [" + taggedValue + "] = "
				+ unTaggedValue);
		assertFalse(checkTag(testValue));
		assertTrue(checkTag(taggedValue));
		assertFalse(checkTag(unTaggedValue));
		assertEquals("Check tag untag", testValue, unTaggedValue);
		assertFalse(testValue.equals(taggedValue));
	}

	private boolean checkTag(String value) throws SecurityException,
	IllegalArgumentException, NoSuchMethodException,
	IllegalAccessException, InvocationTargetException {
		Object[] params = { value };
		return (Boolean) PlethoraTestHelper.privateAccess(protector, "checkTag",
				params);
	}

	@Test
	public void testProtectProcess() {
		String testValue = "opsware_admin";
		String protectedValue = null;
		try {
			protectedValue = protector.protectedValue(testValue);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Protecting failed");
		}
		String plainValue = null;
		try {
			plainValue = protector.plainValue(protectedValue);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unprotecting failed");
		}
		System.out.println("Protecting " + testValue + " = [" + protectedValue
				+ "] = " + plainValue);
		assertEquals("Check protect unprotect", testValue, plainValue);
		assertFalse(testValue.equals(protectedValue));
		try {
			assertFalse("Check is plain", protector.isProtected(testValue));
			assertTrue("Check is protected",
					protector.isProtected(protectedValue));
			assertFalse("Check is plain", protector.isProtected(plainValue));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Protect checks failed");
		}
	}

}
