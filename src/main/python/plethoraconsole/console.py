import curses
import datetime
import sys
import time
import traceback
import urllib2

from optparse import OptionParser  
from threading import Thread

win = None
host = "" 
port = 0 
linecount = 0

EXCEPTION = None
EXIT = False
DATA = None 
ERROR_COLOR = 1
HELP = False
VERBOSE = False
USAGE = """

With no options provided, this interactive console is launched.
With following options you can query specific data at command line.

  Usage: %prog hostname port [option] [argument]

  Options:
    -h, --help            show this help message and exit
    -m NAME, --metric=NAME
                          Get the value of named metric
    -a, --all             Get all metrics
    -o NAME, --module=NAME
                          Get all metrics for the named module
    -l TYPE, --list=TYPE  list names of type: 'metrics' or 'modules' [default:
                          metrics]
""" 

def getMetrics():
    global DATA
    global EXCEPTION
    url = "http://%s:%i/getall/" % (host, port)
    try:
        response = urllib2.urlopen(url)
        #data = response.read()
        #data = data[1:-1] # skip surrounding { } 
        #DATA = data.split(',')
        DATA = sorted(toList(response.read()))
        return True
    except Exception:
        exc_type, value, trace = sys.exc_info()
        EXCEPTION = [] 
        EXCEPTION.append(value)
        EXCEPTION.append(exc_type)
        EXCEPTION.extend(traceback.format_tb(trace, 4))
        DATA = None 
    return False

def toList(data):
    if data is None:
        return []
    stripped = data[1:-1] # skip surrounding { } 
    return stripped.split(',')
        
def printList(data):
    for item in sorted(toList(data)):
        print item.strip()
        
def getMetric(name):
    url = "http://%s:%i/get/%s" % (host, port, name)
    try:
        response = urllib2.urlopen(url)
        print response.read()
    except Exception:
        printException("Failed connecting to %s:%i ..." % (host, port))
        
def getModule(name):
    url = "http://%s:%i/getallfor/%s" % (host, port, name)
    try:
        response = urllib2.urlopen(url)
        printList(response.read())
    except Exception:
        printException("Failed connecting to %s:%i ..." % (host, port))
        
def getAllMetrics():
    url = "http://%s:%i/getall" % (host, port)
    try:
        response = urllib2.urlopen(url)
        printList(response.read())
    except Exception:
        printException("Failed connecting to %s:%i ..." % (host, port))
        
def listModules():
    url = "http://%s:%i/listmodules" % (host, port)
    try:
        response = urllib2.urlopen(url)
        printList(response.read())
    except Exception:
        printException("Failed connecting to %s:%i ..." % (host, port))
        
def listMetrics():
    url = "http://%s:%i/listmetrics" % (host, port)
    try:
        response = urllib2.urlopen(url)
        printList(response.read())
    except Exception:
        printException("Failed connecting to %s:%i ..." % (host, port))
        
def  printException(msg):
    print ""
    print msg 
    print ""
    exc_type, value, trace = sys.exc_info()
    print value
    print exc_type 
    if VERBOSE:
        traces = traceback.format_tb(trace)
        for line in traces:
            print line
    
def drawException():
    if EXCEPTION is None:
        return
    drawNextLine("")
    for item in EXCEPTION:
        drawNextLine(">    %s" % item)
    drawNextLine("")
    
def drawLine(x, y, msg, attr=None):
    try:
        if attr is None:
            win.addstr(x, y, clearDraw(msg))
        else:
            win.addstr(x, y, clearDraw(msg), attr)
    except:
        pass
    
def drawNextLine(msg, attr=None):
    global linecount
    drawLine(linecount, 0, msg, attr)
    linecount += 1
    
def drawRedLine(msg):
    drawNextLine(msg, curses.color_pair(ERROR_COLOR))
    
def drawBoldLine(msg):
    drawNextLine(msg, curses.A_BOLD)
    
def drawHLine(length):
    drawNextLine('-' * length)
        
def drawError(msg):
    drawRedLine(msg)
    
def clearDraw(msg):
    """hack to fill whitespace for the window width instead of erasing line with
    redrawline(beg,end), which is not working with refresh()
    """
    if msg is None:
        return (" " * MY)
    if len(msg) >= MY-1:
        return msg
    #msg = msg.strip() # remove end of lines
    #msg = msg[:(MY-1)]    # cut to window size
    filler = (MY-1) - len(msg)
    return msg + (" " * filler)
    
def clearWin():
    global linecount
    clearcount = linecount
    linecount = 0
    #for line in range(0, MX-1):
    for _ in range(0, clearcount):
        drawNextLine("")
    linecount = 0
    drawHeader()
    drawWin()
    
def drawWin():
    win.refresh()
    
def drawHeader():
    title = "Plethora Monitoring Console (Beta)| %s" % datetime.datetime.now()
    drawBoldLine(title)
    drawHLine(MY-1)
    
def drawFooter(data=None):
    msg = "[Esc] to close conosle | [h]elp "
    if data is not None:
        msg = data
    drawLine(MX-2, 0, clearDraw('-' * (MY-1)))
    drawLine(MX-1, 0, clearDraw(msg))

def displayMetrics():
    counter = 1
    for item in DATA:
        pair = item.strip().split('=')    
        drawNextLine("%2i | %s = %s" % (counter, pair[0], pair[1]))
        counter += 1
    drawFooter()

alternator = False
def displayError():
    global alternator
    if alternator:
        drawNextLine("Retrying connection to %s:%i ..." % (host, port))
        alternator = False
    else:
        drawError("Failed connecting to metrics server !")
        drawException()
        alternator = True
    drawFooter()

def displayHelp():
    if USAGE is  None:
        drawNextLine("sorry, no help found.")
    else:
        lines = USAGE.split('\n')
        for line in lines: 
            drawNextLine(line)
    drawFooter("[e]xit help ...")

def display():
    clearWin()
    if HELP:
        displayHelp()
        return 
    if DATA is None: 
        displayError()
    else:
        displayMetrics()
        
def resizeHandler():
    global MX
    global MY
    MX, MY = win.getmaxyx()
    win.erase()
    win.refresh()
    display()

def mainDisplay():
    global HELP
    HELP = False
    
def showHelp():
    global HELP
    HELP = True
    
def handleInput(c): 
    global EXIT
    if c >= 0:
        #drawFooter("%s pressed" % c) #debug
        if c == curses.KEY_RESIZE:
            #drawFooter("%s resized" % c) #debug
            resizeHandler();
        elif c == 27:
            EXIT = True
        elif c == 104 or c == 72:
            showHelp()
        elif c == 101 or c == 69:
            mainDisplay()
           
def inputTask(delay):
    while not EXIT:
        handleInput(win.getch())
        time.sleep(delay)
        
def displayTask(delay):
    getMetrics() # get initial server status once
    while not EXIT:
        display()
        time.sleep(delay)
        
def dataTask(delay):
    while not EXIT:
        getMetrics()
        time.sleep(delay)
        
def startTaskThread(task, delay):
    thread = Thread(target=task, args=(delay,))
    thread.start()
    
def stayAlive(delay):
    while not EXIT:
        time.sleep(delay)

def initCurses():
    global win
    global MX
    global MY
    win = curses.initscr()
    MX, MY = win.getmaxyx()
    win.nodelay(1)
    curses.start_color()
    curses.use_default_colors()
    curses.init_pair(1, curses.COLOR_RED, curses.COLOR_WHITE)
    curses.noecho()
    curses.cbreak()

def closeCurses():
    curses.echo()
    curses.nocbreak()
    curses.endwin()
    
def launchConsole():
    global EXIT
    initCurses()
    try:
        startTaskThread(dataTask, 1)
        startTaskThread(inputTask, .01)
        displayTask(1)
    except KeyboardInterrupt:
        EXIT = True
    closeCurses()
    print ""
     
def printBadArguments(msg, parser):
        print ""
        print "ERROR: %s" % msg
        print ""
        parser.print_help()
    
def processArgs(parser):
    global host
    global port
    _, args = parser.parse_args()
    if len(args) < 2:
        printBadArguments("Please provide 'hostname' and 'port'", parser)
        return False
    else:
        host = args[0]
        try:
            port = long(args[1])
        except:
            printBadArguments("Please provide a valid number for 'port'", parser)
            return False
    return True
        
def handleArgs():
    global VERBOSE 
    use = """Usage: %prog hostname port [option] [argument]
  hostname and port of the metrics server are required arguments.
  If no options are provided, the interactive console will be launched.
"""
    parser = OptionParser(usage = use)
    parser.add_option("-m", "--metric", dest="metric", metavar="NAME", help="Get the value of named metric")
    parser.add_option("-a", "--all", dest="metrics", action="store_true", default=False, help="Get all metrics")
    parser.add_option("-o", "--module", dest="module", metavar="NAME", help="Get all metrics for the named module")
    parser.add_option("-l", "--list", dest="listdata", metavar="TYPE", help="list names of type: 'metrics' or 'modules' [default: metrics]")
    parser.add_option("-v", "--verbose", dest="verbose", action="store_true", default=False, help="Verbose error messages")
    #USAGE = parser.get_usage()
    if processArgs(parser):
        options, _ = parser.parse_args()
        if options.verbose:
            VERBOSE = True
        if options.metric is not None:
            getMetric(options.metric)
        elif options.module is not None:
            getModule(options.module)
        elif options.metrics:
            getAllMetrics()
        elif options.listdata is not None:
            if options.listdata == "modules":
                listModules()
            elif options.listdata == "metrics":
                listMetrics()
            else: 
                printBadArguments("%s is not valid list type, valid types are 'metrics' or 'modules'" % options.listdata, parser)
                return
        else:
            launchConsole()

def main(): 
    handleArgs()

if __name__ == '__main__':
    main()
