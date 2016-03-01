package com.hackorama.plethora;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/* For running a set of selected tests instead of the all tests from the ant build.xml batchtest target */

@RunWith(Suite.class)
@SuiteClasses({ com.hackorama.plethora.server.web.handler.DynamicPageHandlerTest.class,
	com.hackorama.plethora.server.snmp.SnmpAgentTest.class })
public class ServerTestSuite {

}
