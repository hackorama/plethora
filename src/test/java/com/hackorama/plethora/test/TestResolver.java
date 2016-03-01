package com.hackorama.plethora.test;


public class TestResolver {

	public static void main(String[] args) {
		TestResolver test = new TestResolver();
		System.out.println("Should FAIL");
		test.test(".");
		test.test(".hackorama");
		test.test(".hackorama.");
		test.test(".h.p.");
		System.out.println("Should PASS");
		test.test("hackorama");
		test.test("hackorama.");
		test.test("hackorama...");
		test.test("hackorama.");
		test.test("hackorama.dos");
		test.test("hackorama.dos.load");
		test.test("hackorama.dos.load.foo");
	}

	public TestResolver() {

	}

	private void test(String name) {
		testJMX("uno");
		testJMX("dos");
		testJMX("");
		testJMX("fail");
		testJMX(null);
	}

	private void testJMX(String name) {
	}
}
