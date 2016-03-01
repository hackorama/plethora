/***

cd /opt/plethora/doc/examples/java 

javac -cp /opt/plethora/lib/plethora.jar:/opt/plethora/lib/jmx/jmx.jar  HelloPlethoraClient.java
java -cp /opt/plethora/lib/plethora.jar:/opt/plethora/lib/jmx/jmx.jar:.  HelloPlethoraClient

now let plethora server know about this, by adding the connection details of the tres server

# vi /opt/plethora/etc/plethora.properties
jmxmodule.jhello.host = localhost
jmxmodule.jhello.port = 9001
#

restart plethora server

# /opt/plethora/bin/plethoradaemon stop
# /opt/plethora/bin/plethoradaemon start

you should see the connection being made in the server logs

# tail -f /opt/plethora/log/server.log.0

access the console 

# /opt/plethora/bin/console localhost 9999 

access the web console 

http://localhost:88888
 
***/

import java.util.Date;
import java.text.SimpleDateFormat;

import com.hackorama.plethora.channel.Plethora;

public class HelloPlethoraClient {

	public static void main(String[] argv) {
		new HelloPlethoraClient().startModule();
	}

	public HelloPlethoraClient() {

	}

	public void startModule() {
		/*
		 * Initialise plethora with the name of the module : jdemo the configg
		 * file : jdemo.properties the logger to use : passing null logs will go
		 * to console
		 */
		Plethora.initPlethora("jhello", "jhello.properties", null);
		// updating metrics defined in jhello.properties
		Plethora.setMetric("hellomsg", "hello world ");
		Plethora.incrMetric("hellocount");
		runServer();
	}

	/*
	 * simulating a module server that keeps updating metrics every so often
	 */
	private void runServer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Plethora.setMetric("hellomsg",
						"java hello world " + dateFormat.format(new Date()));
				Plethora.incrMetric("hellocount");
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
