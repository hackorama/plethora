"""Test plethora functionality from an application module 


# vi /opt/plethora/etc/plethora.properties
webmodule.tres.host = localhost
webmodule.tres.port = 1003
#

Restart plethora server 

# /opt/plethora/bin/plethoradaemon stop
# /opt/plethora/bin/plethoradaemon start

You should see the connection being made in the logs 

# tail -f /opt/plethora/log/server.log.0

access the console 

# /opt/plethora/bin/console localhost 9999 

access the web console 

http://localhost:9999

"""
import datetime
import sys
import time
import logging

from plethoralib import plethora

plethora.initPlethora("pyhello", "pyhello.properties", logging.getLogger(__name__));
while True:
    # updating metrics defined in pyhello.properties
    plethora.incrMetric("hellocount")
    plethora.setMetric("hellomsg", "python hello world  %s" % datetime.datetime.now())
    time.sleep(5)
    print "."
