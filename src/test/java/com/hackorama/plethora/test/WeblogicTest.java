package com.hackorama.plethora.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WeblogicTest {

	private static String NOTE = "Diagnostics V0.4 09/2010";
	private static String SEPARATOR = ",";
	private static String COMMENT = "#";
	private static String CACHE_OPTION = "cache";
	private static String TRANSACTION_OPTION = "transaction";

	private static int ALL = 0;
	private static int TRANSACTION = 1;
	private static int CACHE = 2;

	private Context ctx;
	private MBeanServer mbs;
	private static JMXConnector connector;
	private static MBeanServerConnection connection;
	private static ObjectName service;
	private final int counter;

	public WeblogicTest() {
		counter = 0;
	}

	public void doDiag(String[] args) {
		if (args.length < 4) {
			printUsage();
		} else {
			int option = ALL;
			if (args.length == 5) {
				option = parseOpts(args[4]);
			}
			startDiag(args[0], args[1], args[2], args[3], option);
		}
	}

	private void printUsage() {
		System.out.println();
		System.out .println("Usage  : \tjava WeblogicTest host port user password [cache|transaction]");
		System.out.println();
		System.out .println("         \tcache : Collect cache/pool/transaction metrics per Entity EJB");
		System.out .println("         \ttransaction : Collect all transaction metrics on the server");
		System.out.println();
		System.out .println("         \tIf no option is specified both \"cache\" and \"transaction\" " + "metrics are collected");
		System.out.println();
		System.out.println("Example: \tjava " + this.getClass().getName() + " server.domain.com 7777 admin secret cache");
		System.out.println("         \tjava " + this.getClass().getName() + " server.domain.com 7777 admin secret transaction");
		System.out.println("         \tjava " + this.getClass().getName() + " server.domain.com 7777 admin secret ");
		System.out.println();
		System.out.println(NOTE);
		System.out.println();
	}

	private int parseOpts(String option) {
		if (option.equals(TRANSACTION_OPTION)) {
			return TRANSACTION;
		}
		if (option.equals(CACHE_OPTION)) {
			return CACHE;
		}
		return ALL;
	}

	private void timeStamp(String host, String port, String username) {
		String ip = null;
		String hostname = null;

		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
		}

		Date now = new Date();
		System.out.println(COMMENT + host + SEPARATOR + port + SEPARATOR
				+ username + SEPARATOR + hostname + SEPARATOR + ip + SEPARATOR
				+ now.toString() + SEPARATOR + NOTE);
	}

	private static long KILO = 1024;
	private static long MEGA = KILO * KILO;
	private static long GIGA = MEGA * KILO;

	private String formatSize(long size) {
		// if( size > GIGA ) return new String( (float)((float)size/(float)GIGA)
		// + "GB");
		if (size > MEGA) {
			return new String(size / MEGA + "MB");
		}
		if (size > KILO) {
			return new String(size / KILO + "KB");
		}
		return new String(size + "B");
	}

	private String formatName(String name) {
		try {
			Pattern p = Pattern.compile("\\/");
			String[] items1 = p.split(name);
			p = Pattern.compile("\\.");
			String[] items2 = p.split(items1[1]);
			return items2[0];
		} catch (Exception e) {
			return name; // if anything fails, return original name
		}
	}

	private void startDiag(String host, String port, String username,
			String password, int option) {
		ctx = null;

		timeStamp(host, port, username);
		if (initContext(host, port, username, password)) {
			diagTest();
			/*
			if (option == ALL || option == CACHE) {
				diagEntityBeans();
			}
			if (option == ALL || option == TRANSACTION) {
				diagTransactions();
			}
			diagJVM();
			 */
		}
		timeStamp(host, port, username);
	}

	private boolean _initContext(String host, String port, String username,
			String password) {
		String protocol = "t3";
		Integer portInteger = Integer.valueOf(port);
		int portNumber = portInteger.intValue();
		String jndiroot = "/jndi/";
		String mserver = "weblogic.management.mbeanservers.domainruntime";
		JMXServiceURL serviceURL;
		try {
			serviceURL = new JMXServiceURL(protocol, host, portNumber, jndiroot
					+ mserver);
			/*
			 * serviceURL = new JMXServiceURL("service:jmx:iiop://" + host + ":"
			 * + port + "/jndi/weblogic.management.mbeanservers.runtime");
			 */

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		Map<String, Object> env = new HashMap<String, Object>();
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
				"weblogic.management.remote");
		env.put("jmx.remote.x.request.waiting.timeout", new Long(10000));
		try {
			connector = JMXConnectorFactory.connect(serviceURL, env);
			connection = connector.getMBeanServerConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			service = new ObjectName(
					"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void diagTest() {
		try {
			printNameAndState();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ObjectName[] getServerRuntimes() throws Exception {
		return (ObjectName[]) connection
				.getAttribute(service, "ServerRuntimes");
	}

	public void printNameAndState() throws Exception {
		ObjectName[] serverRT = getServerRuntimes();
		System.out.println("got server runtimes");
		int length = serverRT.length;
		for (int i = 0; i < length; i++) {
			String name = (String) connection.getAttribute(serverRT[i], "Name");
			String state = (String) connection.getAttribute(serverRT[i],
					"State");
			System.out.println("Server name: " + name + ".   Server state: "
					+ state);
		}
	}

	private boolean initContext(String host, String port, String username,
			String password) {
		String url = "t3://" + host + ":" + port;

		Properties prop = new Properties();
		prop.put(Context.INITIAL_CONTEXT_FACTORY,
				"weblogic.jndi.WLInitialContextFactory");
		prop.put(Context.PROVIDER_URL, url);
		prop.put(Context.SECURITY_PRINCIPAL, username);
		prop.put(Context.SECURITY_CREDENTIALS, password);

		try {
			ctx = new InitialContext(prop);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		try {
			mbs = (MBeanServer) ctx.lookup("java:comp/env/jmx/domainRuntime");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		if (mbs == null) {
			System.out.println("ERROR : Could not initialise context");
			return false;
		}
		return true;
	}

	/*
	private List diagDeployed(String serverName) {
		List result = new ArrayList();
		ServerRuntimeMBean serverRuntimeMBean = null;
		Set mbeanSet;
		Iterator mbeanIterator;

		ObjectName[] objectNames = (ObjectName[]) connection.getAttribute(
				service, "ServerRuntimes");
		for (ObjectName name : objectNames) {
			System.out.println("Application name: "
					+ (String) connection.getAttribute(name, "Name"));
			serverRuntimeMBean = (ServerRuntimeMBean) name;
		}

		if (serverRuntimeMBean != null) {
			System.out.println("Scanning " + serverRuntimeMBean.getDefaultURL()
					+ " ...");
			ApplicationRuntimeMBean[] apps = serverRuntimeMBean
					.getApplicationRuntimes();
			for (ApplicationRuntimeMBean app : apps) {
				ComponentRuntimeMBean[] components = app.getComponentRuntimes();
				for (ComponentRuntimeMBean bean : components) {
					// if (bean.getType().equals("EJBComponentRuntime")) {
					result.add(bean);
					System.out.println("* " + bean.getModuleId());
					// } else {
					// System.out.println("  " + bean.getDefaultURL());
					// }
				}
			}
		}
		return result;
	}

	private void displayEQDiag(ExecuteQueueRuntimeMBean EQRMBean) {
		try {
			System.out.println(EQRMBean.hashCode() + ", "
					+ EQRMBean.getExecuteThreadCurrentIdleCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void diagEQ() {
		ExecuteQueueRuntimeMBean EQRMBean;
		Set mbeanSet;
		Iterator mbeanIterator;

		ObjectName[] objectNames = (ObjectName[])  connection.getAttribute(service, "ExecuteQueueRuntime");
		for (ObjectName name : objectNames) {
			EQRMBean = (ExecuteQueueRuntimeMBean) name;
			displayEQDiag(EQRMBean);
		}
	}

	private void displayEntityDiag(EntityEJBRuntimeMBean entityBean) {
		try {
			System.out.print(counter + SEPARATOR);
			System.out.print(formatName(entityBean.getEJBName())
					+ SEPARATOR);

			EJBCacheRuntimeMBean cache = entityBean.getCacheRuntime();

			System.out.print(cache.getCachedBeansCurrentCount() + SEPARATOR);
			System.out.print(cache.getCacheAccessCount() + SEPARATOR);
			System.out.print(cache.getCacheHitCount() + SEPARATOR);
			System.out.print(cache.getCacheMissCount() + SEPARATOR);
			System.out.print(cache.getActivationCount() + SEPARATOR);
			System.out.print(cache.getPassivationCount() + SEPARATOR);

			EJBPoolRuntimeMBean pool = entityBean.getPoolRuntime();

			System.out.print(pool.getPooledBeansCurrentCount() + SEPARATOR);
			System.out.print(pool.getAccessTotalCount() + SEPARATOR);
			System.out.print(pool.getDestroyedTotalCount() + SEPARATOR);
			System.out.print(pool.getIdleBeansCount() + SEPARATOR);
			System.out.print(pool.getBeansInUseCount() + SEPARATOR);
			System.out.print(pool.getBeansInUseCurrentCount() + SEPARATOR);
			System.out.print(pool.getWaiterTotalCount() + SEPARATOR);
			System.out.print(pool.getWaiterCurrentCount() + SEPARATOR);
			System.out.print(pool.getTimeoutTotalCount() + SEPARATOR);

			EJBTransactionRuntimeMBean tx = entityBean.getTransactionRuntime();

			System.out.print(tx.getTransactionsCommittedTotalCount()
					+ SEPARATOR);
			System.out.print(tx.getTransactionsRolledBackTotalCount()
					+ SEPARATOR);
			System.out.print(tx.getTransactionsTimedOutTotalCount());
		} catch (Exception e) {
			System.out.println("ERROR @ displayEntityDiag");
			e.printStackTrace();
		} finally {
			System.out.println();
		}
	}

	private void diagEntityBeans() {
		EntityEJBRuntimeMBean entityBean;
		Set mbeanSet;
		Iterator mbeanIterator;

		System.out.print(COMMENT);
		System.out.print("Count" + SEPARATOR);
		System.out.print("Type" + SEPARATOR);
		System.out.print("Bean" + SEPARATOR);

		System.out.print("CachedBeansCurrentCount" + SEPARATOR);
		System.out.print("CacheAccessCount" + SEPARATOR);
		System.out.print("CacheHitCount" + SEPARATOR);
		System.out.print("CacheMissCount" + SEPARATOR);
		System.out.print("ActivationCount" + SEPARATOR);
		System.out.print("PassivationCount" + SEPARATOR);

		System.out.print("PooledBeansCurrentCount" + SEPARATOR);
		System.out.print("AccessTotalCount" + SEPARATOR);
		System.out.print("DestroyedTotalCount" + SEPARATOR);
		System.out.print("IdleBeansCount" + SEPARATOR);
		System.out.print("BeansInUseCount" + SEPARATOR);
		System.out.print("BeansInUseCurrentCount" + SEPARATOR);
		System.out.print("WaiterTotalCount" + SEPARATOR);
		System.out.print("WaiterCurrentCount" + SEPARATOR);
		System.out.print("TimeoutTotalCount" + SEPARATOR);

		System.out.print("TransactionsCommittedTotalCount" + SEPARATOR);
		System.out.print("TransactionsRolledBackTotalCount" + SEPARATOR);
		System.out.println("TransactionsTimedOutTotalCount");

		counter = 1;
		ObjectName[] objectNames = (ObjectName[]) connection.getAttribute(
				service, "EntityEJBRuntime");
		for (ObjectName name : objectNames) {
			entityBean = (EntityEJBRuntimeMBean) name;
			displayEntityDiag(entityBean);
			counter++;
		}
	}

	private void displayJVMBean(JVMRuntimeMBean jvmBean) {
		long max = jvmBean.getHeapSizeMax();
		long current = jvmBean.getHeapSizeCurrent();
		long free = jvmBean.getHeapFreeCurrent();
		long freepercent = jvmBean.getHeapFreePercent();

		System.out.print(COMMENT);
		System.out.print("Heap Max" + SEPARATOR);
		System.out.print("Heap Current" + SEPARATOR);
		System.out.print("Heap Free" + SEPARATOR);
		System.out.print("Heap Free %" + SEPARATOR);
		System.out.print("Heap Max" + SEPARATOR);
		System.out.print("Heap Current" + SEPARATOR);
		System.out.println("Heap Free");

		System.out.print(max + SEPARATOR);
		System.out.print(current + SEPARATOR);
		System.out.print(free + SEPARATOR);
		System.out.print(freepercent + SEPARATOR);
		System.out.print(formatSize(max) + SEPARATOR);
		System.out.print(formatSize(current) + SEPARATOR);
		System.out.println(formatSize(free));
	}

	private void diagJVM() {
		JVMRuntimeMBean jvmBean;
		Set mbeanSet;
		Iterator mbeanIterator;

		mbeanSet = home.getMBeansByType("JVMRuntime");
		mbeanIterator = mbeanSet.iterator();
		while (mbeanIterator.hasNext()) {
			jvmBean = (JVMRuntimeMBean) mbeanIterator.next();
			displayJVMBean(jvmBean);
		}
	}

	private void displayTxBean(TransactionNameRuntimeMBean txBean) {
		System.out.print(counter + SEPARATOR);
		System.out.print(txBean.getTransactionName() + SEPARATOR);
		System.out.print(txBean.getTransactionTotalCount() + SEPARATOR);
		System.out.print(txBean.getSecondsActiveTotalCount() + SEPARATOR);
		System.out
				.print(txBean.getTransactionCommittedTotalCount() + SEPARATOR);
		System.out
				.print(txBean.getTransactionAbandonedTotalCount() + SEPARATOR);
		System.out.print(txBean.getTransactionRolledBackTotalCount()
				+ SEPARATOR);
		System.out.print(txBean.getTransactionRolledBackTimeoutTotalCount()
				+ SEPARATOR);
		System.out.print(txBean.getTransactionRolledBackAppTotalCount()
				+ SEPARATOR);
		System.out.print(txBean.getTransactionRolledBackSystemTotalCount()
				+ SEPARATOR);
		System.out.print(txBean.getTransactionRolledBackResourceTotalCount()
				+ SEPARATOR);
		System.out.println(txBean.getTransactionHeuristicsTotalCount());
	}

	private void diagTransactions() {
		String SAVED_SEPARATOR = SEPARATOR;
		SEPARATOR = "|"; // tx names includes "," so we cant use it as delimiter

		TransactionNameRuntimeMBean txBean;
		Set mbeanSet;
		Iterator mbeanIterator;

		System.out.print(COMMENT);
		System.out.print("Count" + SEPARATOR);
		System.out.print("TransactionName" + SEPARATOR);
		System.out.print("SecondsActiveTotalCount" + SEPARATOR);
		System.out.print("TransactionCommittedTotalCount" + SEPARATOR);
		System.out.print("TransactionAbandonedTotalCount" + SEPARATOR);
		System.out.print("TransactionRolledBackAppTotalCount" + SEPARATOR);
		System.out.print("TransactionRolledBackResourceTotalCount" + SEPARATOR);
		System.out.print("TransactionRolledBackSystemTotalCount" + SEPARATOR);
		System.out.print("TransactionRolledBackTimeoutTotalCount" + SEPARATOR);
		System.out.print("TransactionRolledBackTotalCount" + SEPARATOR);
		System.out.print("TransactionTotalCount" + SEPARATOR);
		System.out.println("TransactionHeuristicsTotalCount");

		mbeanSet = home.getMBeansByType("TransactionNameRuntime");
		mbeanIterator = mbeanSet.iterator();
		counter = 1;
		while (mbeanIterator.hasNext()) {
			txBean = (TransactionNameRuntimeMBean) mbeanIterator.next();
			displayTxBean(txBean);
			counter++;
		}
		SEPARATOR = SAVED_SEPARATOR;
	}
	 */

	public static void main(String[] args) {
		new WeblogicTest().doDiag(args);
	}
}
