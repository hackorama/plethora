package com.hackorama.plethora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlethoraTestHelper {

	/**
	 * Helper to test private methods from an object under test
	 */
	public static Object privateAccess(Object testObject,
			String testMethodName, Object[] testMethodParams)
					throws SecurityException, NoSuchMethodException,
					IllegalArgumentException, IllegalAccessException,
					InvocationTargetException {
		// getDeclaredMethod matches only exact type, instead
		// check matching interfaces and super param types one by one
		for (Method method : testObject.getClass().getDeclaredMethods()) {
			if (method.getName().equals(testMethodName)) {
				if (method.getParameterTypes().length == testMethodParams.length) {
					boolean isParamTypesMatching = true;
					int i = 0;
					for (Class<?> paramType : method.getParameterTypes()) {
						if (!paramType.isAssignableFrom(testMethodParams[i++]
								.getClass())) {
							isParamTypesMatching = false;
						}
					}
					if (isParamTypesMatching) {
						method.setAccessible(true);
						return method.invoke(testObject, testMethodParams);
					}
					isParamTypesMatching = false;
				}
			}
		}
		throw new NoSuchMethodException(testMethodName);
	}

	/**
	 * Helper to access private fields of an object under test
	 */
	public static void privateFieldAccess(Object testObject, String fieldName,
			Object fieldValue) throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field = testObject.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(testObject, fieldValue);

	}

	public static String genTestFile(String str) {
		return genTestFile(str, ".tmp");
	}

	public static String genTestFile(String str, String extension) {
		PrintWriter writer = null;
		File file = null;
		try {
			file = File.createTempFile("plethora_test_", extension);
			writer = new PrintWriter(file);
			writer.println(str);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		return file == null ? null : file.getAbsolutePath();
	}

	public static String getFileContent(String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = reader.readLine()) != null){
				stringBuilder.append(line).append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return stringBuilder.toString();
	}

	/**
	 * windows paths will be formatted as common java path
	 * http://docs.oracle.com/javase/tutorial/essential/io/pathOps.html 
	 * In JDK 7 use Paths instead
	 */
	public static String portableFilePath(String fileName) {
		return fileName.replace("\\", "/");

	}
}
