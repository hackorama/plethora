Plethora
====

[![Build Status](https://travis-ci.org/hackorama/plethora.svg?branch=master)](https://travis-ci.org/hackorama/plethora)
[![codecov.io](https://codecov.io/github/hackorama/plethora/coverage.svg?branch=master)](https://codecov.io/github/hackorama/plethora?branch=master)

![tag logo](https://github.com/hackorama/plethora/blob/master/src/main/resources/web/img/logo.png)

Application metrics collection framework to gather metrics from each module of a distributed polyglot application.
Built initially for java ecosystem using JMX protocol extended to work with multiple platforms (Java, Python, C++, Go etc.) and multiple protocols JMX, SNMP, HTTP. 

![Plethora Web Console](https://github.com/hackorama/plethora/blob/master/doc/images/plethora-web-console.png)

## Getting Started

### Download source and build 

    $ java -version
    java version "1.7.0_95"
    $

    $ git clone https://github.com/hackorama/plethora
    $ cd plethora/

    $ ./gradlew plethora
    ...
    BUILD SUCCESSFUL
    $

### Run Plethora 

Out of the box, the Plethora server collects few system metrics from the server Plethora is running.

(We have to configure application modules using Plethora client to expose additional application metrics)

    $ ./gradlew runServer
    ...
    INFO: Not part of a cluster, no cluster information metrics added to plethora module
    INFO: Meta module plethora ready
    INFO: System module system ready
    INFO: Plethora Service Controller : connection manager service scheduled at 60 second intervals
    INFO: Plethora Service Controller : data refresh service scheduled at 5 second intervals
    INFO: Plethora Service Controller : data refresh service scheduled at 5 second intervals
    INFO: JMX agent started for plethora at localhost:9998 (service:jmx:rmi:///jndi/rmi://localhost:9998/jmxrmi)
    INFO: Started JMX agent server at localhost:9998
    INFO: Started HTTP web server at localhost:9999
    INFO: SNMP MIB description written to : src/main/resources/web//snmp/mib
    INFO: Started  SNMP agent server at localhost:9997
    INFO: Service Status
    
    Active task thread groups
    PlethoraModuleService-2
    PlethoraMetricService-3
    PlethoraHTTPServer-4
    PlethoraSNMPServer-5
    
    Active task threads
    9 PlethoraModuleService-2-1     TIMED_WAITING
    10 PlethoraMetricService-3-1    WAITING
    11 PlethoraMetricService-3-2    TIMED_WAITING
    23 PlethoraSNMPServer-5-1       RUNNABLE
    
    Plethora Server Ready


### Interact using HTTP/JMX/SNMP

From another console interact with running server using HTTP, JMX, SNMP interfaces.

#### HTTP

    $ curl http://localhost:9999/get/hackorama.plethora.server_name
    Plethora Metrics Server
    $ curl http://localhost:9999/get/hackorama.system.memory
    53
    $

#### JMX

    $ wget http://downloads.sourceforge.net/cyclops-group/jmxterm-1.0-alpha-4-uber.jar

    $ cat plethora.jmx
    domain plethora
    bean name=plethora
    get hackorama.plethora.server_name
    get hackorama.system.memory
    $

    $ java -jar jmxterm-1.0-alpha-4-uber.jar -v silent --url localhost:9998 -i plethora.jmx
    hackorama.plethora.server_name = Plethora Metrics Server;
    hackorama.system.memory = 36;
    $

#### SNMP

    $ sudo apt-get install snmp
    $ sudo apt-get install snmp-mibs-downloader

    $ wget http://localhost:9999/snmp/mib -O ~/.snmp/mibs/PLETHORA-MIB.txt

    $ snmpwalk -m +PLETHORA-MIB  -v2c -c community  localhost:9997 .1.3.6.1.4.1.11.4.999
    PLETHORA-MIB::plethora = STRING: "plethora"
    PLETHORA-MIB::clusterhostname = ""
    PLETHORA-MIB::clusterport = Wrong Type (should be OCTET STRING): INTEGER: 0
    PLETHORA-MIB::httphostname = STRING: "0.0.0.0"
    PLETHORA-MIB::httpport = Wrong Type (should be OCTET STRING): INTEGER: 9999
    PLETHORA-MIB::jmxhostname = STRING: "0.0.0.0"
    PLETHORA-MIB::jmxport = Wrong Type (should be OCTET STRING): INTEGER: 9998
    PLETHORA-MIB::servername = STRING: "Plethora Metrics Server"
    PLETHORA-MIB::serverreleasedate = ""
    PLETHORA-MIB::serverversion = STRING: "0.1"
    PLETHORA-MIB::snmphostname = STRING: "0.0.0.0"
    PLETHORA-MIB::snmpport = Wrong Type (should be OCTET STRING): INTEGER: 9997
    PLETHORA-MIB::system = STRING: "system"
    PLETHORA-MIB::cpu = INTEGER: 1
    PLETHORA-MIB::memory = INTEGER: 44
    SNMPv2-SMI::zeroDotZero = No more variables left in this MIB View (It is past the end of the MIB tree)
    $


### User Interfaces

There are sample CLI Console and Web Console that uses the HTTP interface.

#### Web

http://localhost:9999 

![Plethora Web Console](https://github.com/hackorama/plethora/blob/master/doc/images/plethora-web-console.png)

#### CLI

    $ cd src/main/python/plethoraconsole/
    $ ./console localhost 9999

![Plethora CLI Console](https://github.com/hackorama/plethora/blob/master/doc/images/plethora-cli-console.png)

## Connecting to application modules for metrics

Now we can configure the Plethora client library inside an application module and expose the module's metrics to the Plethora server.

### Java Example

## Build the Plethora client jar

    $ git clone https://github.com/hackorama/plethora
    $ cd plethora/
    $ ./gradlew clientJar
    $ ls build/libs/*client*
    build/libs/plethora_client-1.0.jar
    $

## Build jdemo the java demo application using the client jar and the JMX jar

    $ javac -cp build/libs/plethora_client-1.0.jar:lib/jmx/jmxremote_optional.jar src/test/java/com/hackorama/plethora/examples/DemoAppServer.java

## Run the jdemo application with the demo metrics properties file

    $ java -cp build/libs/plethora_client-1.0.jar:lib/jmx/jmxremote_optional.jar:src/test/java com.hackorama.plethora.examples.DemoAppServer src/test/resources/examples/jdemo.metrics.properties
    INFO: Server MBean : Adding attribute cache_mode
    INFO: Server MBean : Adding attribute connection
    INFO: Server MBean : Adding attribute log_level
    INFO: Server MBean : Adding attribute queue_fill_rate
    INFO: JMX agent started for jdemo at localhost:9001 (service:jmx:jmxmp://localhost:9001)
    INFO: Started channel jmx agent plethora:name=jdemo at localhost:9001
    INFO: Started plethora channel jdemo

## Update Plethora server configuration with the connection for this demo application we just started.

    $ vi src/main/resources/plethora.properties
    ...
    jmxmodule.jdemo.host = localhost
    jmxmodule.jdemo.port = 9001
    ...
    $

## And start or restart Plethora server

You will see log messages about the jdemo module metrics and connection.

    $ ./gradlew runServer
    ...
    INFO: Added metric hackorama.jdemo.cache_mode as TEXT
    INFO: Added metric hackorama.jdemo.connection as NUMBER
    INFO: Added metric hackorama.jdemo.log_level as TEXT
    INFO: Added metric hackorama.jdemo.queue_fill_rate as NUMBER
    INFO: Module jdemo connected
    INFO: JMX module jdemo ready
    ...
    Plethora Server Ready

## Verify the jdemo module metrics are available

    $ curl http://localhost:9999/get/hackorama.jdemo.queue_fill_rate
    42
    $


### Python Example

TODO 
