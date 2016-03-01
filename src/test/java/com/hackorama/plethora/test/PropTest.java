package com.hackorama.plethora.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.hackorama.plethora.channel.PropertiesMetricsReader;
import com.hackorama.plethora.common.data.Metric;
import com.hackorama.plethora.common.data.Metrics;

public class PropTest {

	public void test(String file) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
			System.out.println("java properties");
			List<String> list = new ArrayList<String>();
			for (Object o : props.keySet()) {
				list.add((String) o);
			}
			Collections.sort(list);
			for (String item : list) {
				System.out.println("[" + item + "] = [" + props.getProperty(item) + "]");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void readerTest() {
		Metrics metrics = new PropertiesMetricsReader(Metrics.getInstance())
		.getMetricsFromFile("tests/test.properties");
		for (Entry<String, Metric<?>> item : metrics.getMetrics().entrySet()) {
			System.out.println(item.getKey());
			System.out.println(item.getValue().getName());
		}
	}

	public static void main(String[] args) {
		new PropTest().readerTest();
	}
}