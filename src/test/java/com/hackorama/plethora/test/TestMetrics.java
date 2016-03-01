package com.hackorama.plethora.test;

import com.hackorama.plethora.common.data.Metric;
import com.hackorama.plethora.common.data.Metrics;

public class TestMetrics {

	public static void main(String[] args) {
		new TestMetrics().test();
	}

	Metrics metrics;

	private void negetive() {
		metrics = Metrics.getInstance();

		new Metric<Long>("fail", (long) 0);
		new Metric<Long>("fail.fail", (long) 0);
		new Metric<Long>("fail.fail.fail", (long) 0);

		System.out.println(metrics.getValue("fail"));
		System.out.println(metrics.getValue("fail.fail"));
		System.out.println(metrics.getValue("fail.fail.fail"));
		System.out.println(metrics.getValue("hackorama"));
		System.out.println(metrics.getValue("hackorama.fail"));
		System.out.println(metrics.getValue("hackorama.fail.fail"));
		System.out.println(metrics.getValue("hackorama.uno"));
		System.out.println(metrics.getValue("hackorama.uno.fail"));
	}

	private void positive() {
		new Metric<Long>("hackorama.dos.load", (long) 0);
		new Metric<Long>("hackorama.dos.level", (long) 0);
		new Metric<Long>("hackorama.dos.conflicts", (long) 0);

		new Metric<String>("hackorama.uno.data", "");
		new Metric<Long>("hackorama.uno.load", (long) 0);
		new Metric<Long>("hackorama.tres.level", (long) 0);
		new Metric<String>("hackorama.dos.name", "soda");

		System.out.println("\njmx module tests - mock uno, dos proxies \n");

		System.out.println(metrics.getValue("hackorama.dos.load") + " == 100");
		System.out
		.println(metrics.setValue("hackorama.dos.load", 420) + " == false");
		System.out.println(metrics.getValue("hackorama.dos.load") + " == 100");

		System.out.println(metrics.getValue("hackorama.dos.level") + " == 101 ");
		System.out
		.println(metrics.setValue("hackorama.dos.level", 42) + " == true ");
		System.out.println(metrics.getValue("hackorama.dos.level") + " == 42");

		System.out.println(metrics.getValue("hackorama.dos.conflicts") + " == 102");
		System.out.println(metrics.setValue("hackorama.dos.conflicts", 300)
				+ " == false");
		System.out.println(metrics.getValue("hackorama.dos.conflicts") + " == 102");
		System.out
		.println(metrics.setValue("hackorama.dos.level", 444) + " == true");
		System.out.println(metrics.getValue("hackorama.dos.level") + " == 444");

		System.out.println(metrics.getValue("hackorama.uno.data") + " == 0");
		System.out.println(metrics.getValue("hackorama.uno.load") + " == 0");

		System.out.println("\nweb module tests - mock tres \n");
		System.out.println(metrics.getValue("hackorama.tres.level") + " == 1");
		System.out.println(metrics.setValue("hackorama.tres.level", "fail")
				+ " == false");

		System.out.println("\nunknown modules/metrics error condition tests\n");

		System.out.println(metrics.setValue("hackorama.foo.fail", "fail")
				+ " == false");
		System.out.println(metrics.getValue("hackorama.uno.fail") + " == 0");
		System.out.println(metrics.getValue("hackorama.tres.fail") + " == 0");
		System.out.println(metrics.setValue("hackorama.uno.fail", "fail")
				+ " == false");
		System.out.println(metrics.setValue("hackorama.tres.fail", "fail")
				+ " == false");

		System.out.println("\nmodule data type abuse tests\n");

		// System.out.println(metrics.getTextValue("hackorama.dos.level")
		// + " == null with warning");
		// System.out
		// .println(metrics.getNumberValue("hackorama.dos.level") + " == OK ");
		System.out.println(metrics.getValue("hackorama.dos.level") + " == OK ");
		System.out.println(metrics.setValue("hackorama.dos.level", 1000)
				+ " == true ");
		System.out.println(metrics.setValue("hackorama.dos.level", "fail")
				+ " == false set with warning ");

		System.out.println(metrics.getValue("hackorama.dos.name") + " == soda ");
		//System.out.println(metrics.getTextValue("hackorama.dos.name") + " == soda");
		//System.out.println(metrics.getNumberValue("hackorama.dos.name")
		// + " == 0 with warning");
		System.out.println(metrics.setValue("hackorama.dos.name", "creamsoda")
				+ " == true ");
		System.out.println(metrics.setValue("hackorama.dos.name", 1)
				+ " == false set with warning ");

	}

	public void test() {
	}

	private void waitTill() {
		try {
			System.out.println("Press any key to continue");
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
