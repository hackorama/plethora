#
# @author: kishan.thomas@gmail.com
#
# $ cd tests/scripts
# $ python test_metrics.py 3333 /path/to/plethora.srv  /path/to/plethora.properties
#
import sys, os, random
sys.path.append('../../../src/python')

import logging, socket, os, cgi, time
from threading import Thread
from SocketServer import BaseServer
from BaseHTTPServer import HTTPServer
from SimpleHTTPServer import SimpleHTTPRequestHandler
try:
    from OpenSSL import SSL # get from https://launchpad.net/pyopenssl
except:
    sys.exit("OpenSSL is required, please install from https://launchpad.net/pyopenssl !")
    
import mockdatastore 
from plethoralib import plethora

COMMON_URL = "/plethora.py?"
MODULE="mockmodule"
httpd = None

logging.basicConfig()
LOGGER = logging.getLogger(MODULE)
LOGGER.setLevel(logging.DEBUG)

class SecureHTTPServer(HTTPServer):
    def __init__(self, server_address, cert_file, handler_class):
        BaseServer.__init__(self, server_address, handler_class)
        ctx = SSL.Context(SSL.SSLv23_METHOD)
        ctx.use_privatekey_file (cert_file)
        ctx.use_certificate_file(cert_file)
        self.socket = SSL.Connection(ctx, socket.socket(self.address_family,
                                                        self.socket_type))
        self.server_bind()
        self.server_activate()

class SecureHTTPRequestHandler(SimpleHTTPRequestHandler):
    def setup(self):
        self.connection = self.request
        self.rfile = socket._fileobject(self.request, "rb", self.rbufsize)
        self.wfile = socket._fileobject(self.request, "wb", self.wbufsize)

    def do_GET(self):
        global LOGGER
        plethora.metricsRequestHandler(self, logger=LOGGER) # 3. plethora handle web request
        return
    
    def handle(self): # Hack to ignore SysCallError from SSL socket 
        try:
            SimpleHTTPRequestHandler.handle(self)
        except SSL.SysCallError:
            pass 
        
    def log_message(self, format, *args):
        # no logging 
        pass

def serv(port, certfile):
    global httpd
    try:
        #serveraddress = ('0.0.0.0', port) # (address, port)
        serveraddress = ('localhost', port) # (address, port)
        #serveraddress = ('127.0.0.1', port) # (address, port)
        httpd = SecureHTTPServer(serveraddress, certfile, SecureHTTPRequestHandler)
        cloudy = httpd.socket.getsockname()
        print "Serving tres on", cloudy[0], "port", cloudy[1], "..."
        httpd.serve_forever()
        #input("Press any key to exit ...") 
        #print "Closing mock tres module service ..."
        #server.socket.close()
        #sys.exit(0)
    except KeyboardInterrupt:
        print 'Closing server ...'
        httpd.socket.close()

def keyCheck():
    if raw_input("") is not None:
        print "Closing mock tres module service ..."
        if httpd is not None:
            httpd.socket.close()
        os._exit(1)
        
def exitCheck():
    thread = Thread(target=keyCheck)
    thread.start()
    
def updateMetricTask():
    delay = 5 
    value = 1
    while True:
        time.sleep(delay)
        value = random.randint(1, 500)
        plethora.setMetric("bytes_send", long(value))
        value = random.randint(1, 100)
        plethora.setMetric("bytes_received", long(value))
        value = random.randint(1, 10)
        plethora.setMetric("files_received", long(value))
        value = random.randint(1, 20)
        plethora.setMetric("files_send", long(value))
        
def updateMetrics():
    thread = Thread(target=updateMetricTask)
    thread.start()
    
def usage():
    print 'Usage: %s port  [/path/to/cert/file/plethora.srv]  [/path/to/config/file/plethora.properties]' % sys.argv[0]
     
def args():
    cert = "plethora.srv"
    conf = "tres.plethora.properties"
    port = 9002
    argc = len(sys.argv)
    #if argc < 2:
    #    usage()
    #    sys.exit()
    if argc >= 2: 
        port = int(sys.argv[1])
    if argc >= 3: 
         cert = sys.argv[2]
    if argc >= 4: 
         conf = sys.argv[3]
    if not os.path.exists(cert):
        print 'ERROR: The cert file %s was not found!' % cert
        usage()
        sys.exit()
    if not os.path.exists(conf):
        print 'ERROR: The conf file %s was not found!' % conf
        usage()
        sys.exit()
    return port, cert, conf 
        
if __name__ == '__main__':
    port, cert_file, conf_file = args()
    plethora.initPlethora(MODULE, conf_file, LOGGER) # 1. initialize plethora
    plethora.setMetric("bytes_send", 400)
    plethora.setMetric("bytes_send", "400")
    plethora.setMetric("bytes_send", "foos")
    updateMetrics() # 2. update metrics 
    exitCheck()
    serv(port, cert_file) # start the module web server