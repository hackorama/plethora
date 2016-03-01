"""Plethora data store implementation"""

import sys
import threading
import traceback

from plethoralib import jprop
from plethoralib.common import Common, PlethoraLogger 
from plethoralib.common import MetricLevel, MetricType, MetricAttrib, MetricProperties
from plethoralib import metrics

Logger = PlethoraLogger(None)

THE_PLETHORA = None
THE_METRICS = None

### begin convenience wrapper methods ###

def initPlethora(name, conf, logger):
    """Initialize plethora once."""
    global Logger
    Logger = PlethoraLogger(logger)
    global THE_PLETHORA
    if THE_PLETHORA is None:
        try:
            THE_PLETHORA = Plethora(name, conf, logger)
        except:
            handleException()
    return THE_PLETHORA

def getPlethora():
    """Get the global plethora object."""
    global THE_PLETHORA
    if THE_PLETHORA is None:
        Logger.error("Please initialize plethora first by calling initPlethora(name, datastore, conf, logger) ...")
    return THE_PLETHORA

def getMetrics(datastore, dataprops, logger):
    """Initialize once the web access interface to the metrics"""
    global THE_METRICS
    if THE_METRICS is None:
        THE_METRICS = metrics.Metrics(datastore, dataprops, logger=logger)
    return THE_METRICS

def metricsRequestHandler(request, logger=None, args=None):
    """Web entry point, handles all http request to the web interface (metrics)."""
    getMetrics(getPlethora().getDataStore(), getPlethora().getDataProps(), logger).requestHandler(request, args)
    
def getMetric(name):
    plethora = getPlethora()
    if plethora is not None:
        return plethora.getMetric(name)

def setMetric(name, value):
    plethora = getPlethora()
    if plethora is not None:
        plethora.setMetric(name, value)

def incrMetric(name, delta=1):
    plethora = getPlethora()
    if plethora is not None:
        plethora.incrMetric(name, delta)

def decrMetric(name, delta=1):
    plethora = getPlethora()
    if plethora is not None:
        plethora.decrMetric(name, delta)

def handleException():
    exc_type, value, trace = sys.exc_info()
    msg = "PLETHORA Exception Type: %s \nPLETHORA Exception Description: %s \n" % (exc_type.__name__, value)
    msg += "PLETHORA Stack Trace:"
    trace_list = traceback.format_tb(trace, 4)
    for item in trace_list:
        msg += "\n" + item
    Logger.error(msg)
        
### end convenience  wrapper methods ###

class Metric:
    """
    Holds a single metric name, value and properties 
    When no properties provided, initializes to default properties
    """
        
    def __init__(self, name, value, properties=None):
        self.__name = name
        self.__value = value
        if properties is None:
            self.__properties = MetricProperties()
        else:
            self.__properties = properties
        
    def getProperties(self):
        return self.__properties
    
    def setProperties(self, properties):
        self.__properties = properties
        
    def getName(self):
        return self.__name
    
    def getValue(self):
        return self.__value
    
    def getType(self):
        if Common().isNumber(self.__value):
            return MetricType.NUMBER
        elif Common().isBoolean(self.__value):
            return MetricType.BOOLEAN
        else:
            return MetricType.TEXT
    
    def debugPrint(self):
        properties = self.getProperties()
        print self.__name + "=" +  self.__value + " (" + self.getType()  +")"
        print properties.getDisplayname() + " (" + properties.getLevel() + ", " + str(properties.isReadable()) + ", " + str(properties.isWritable()) +")"
        print properties.getDescription()
        print ""
        
class MetricsPropertiesReader(Common):
    """
    Reads metric definitions from a given java style property file
    
    Mirrors com.hackorama.plethora.channel.PropertiesMetricsReader
    """
    def __init__(self, logger=None):
        global Logger
        self.__metrics = dict()
        self.CONFIG_KEY_SEPARATOR = "."
        if logger is not None: # use the new logger if provided
            Logger = PlethoraLogger(logger) 

    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getMetricsFromFile(propertyfile)
    def getMetricsFromFile(self, propertyfile):
        """
        Read metric definitions from a property file
        Returns a map of all the metrics 
        """
        try:
            self.__processMetricsConfig(propertyfile)
        except:
            Logger.error("Error during getting properties of metric " + str(propertyfile))
            Logger.error(self.handleException())
        return self.__metrics

    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.processMetricsConfig(propertyfile)
    def __processMetricsConfig(self, propertyfile):
        if not self.__isReadableFile(propertyfile):
            Logger.warning("Skipping invalid/unavailable property file : " + str(propertyfile))
            return
        Logger.debug("Reading property file : " + str(propertyfile))
        # TODO i18n/l10n getJavaProperties(codecs.open(propertyfile, 'r', encoding='utf-8')) 
        props = jprop.getJavaProperties(open(propertyfile))
        for key in self.__keySet(props):
            metric = self.__getMetricNameFromPropertyKey(key)
            if not self.__existingMetric(metric):
                value = self.__getProperty(props, metric)
                self.__addMetric(metric, value, self.__getMetricProperties(metric, props))
            else:
                Logger.debug("Skip already added metric/property key : " + str(key))

    # mirrors java.util.Properties.keySet()
    def __keySet(self, props):
        return props.keys()

    # mirrors java.util.Properties.getProperty()
    def __getProperty(self, props, name):
        return props.get(name)
    
    # mirrors com.hackorama.plethora.common.Util.isReadableFile()
    def __isReadableFile(self, filename):
        if filename is None:
            return False
        try:
            f = open(filename)
            f.close()
            return True
        except:
            return False
        return False

    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getMetric(name)
    def __getMetricNameFromPropertyKey(self, key):
        for  attrib in MetricAttrib().values():
            if key.endswith(self.CONFIG_KEY_SEPARATOR + attrib.lower()):
                return key[:key.rfind(self.CONFIG_KEY_SEPARATOR)]
            if key.endswith(self.CONFIG_KEY_SEPARATOR + attrib.upper()):
                return key[:key.rfind(self.CONFIG_KEY_SEPARATOR)]
        return key
    
    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.existingMetric(name)
    def __existingMetric(self, metric):
        return self.__metrics.has_key(metric)
    
    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getType
    def __getType(self, thetype):
        for atype in MetricType().values():
            if atype.lower() == thetype.lower():
                return atype 
        return MetricType.INVALID
    
    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getType
    def __getLevel(self, thelevel):
        for level in MetricLevel().values():
            if level == thelevel.upper() or level == thelevel.lower():
                return level
        return self.DEFAULT_METRIC_LEVEL
    
    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getAttribute
    def __getAttribute(self, attribute, props, metric, defaultvalue):
        result = self.__getProperty(props, metric + self.CONFIG_KEY_SEPARATOR + attribute.lower())
        if result is None:
            result = self.__getProperty(props, metric + self.CONFIG_KEY_SEPARATOR + attribute.upper())
        if result is None:
            return defaultvalue
        else:
            return result.strip()

    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.addMetric
    def __addMetric(self, metric, textvalue, properties):
        thetype = properties.getType()
        if thetype == MetricType.UNSPECIFIED: 
            # for unspecified type derive type from provided initial value
            thetype = self.valueType(textvalue) 
            Logger.info("%s : No metric type specified, using %s based on initial value" % (metric, thetype))
            properties.type(thetype) 
        elif thetype  == MetricType.INVALID: 
            # for invalid type derive type from provided initial value
            thetype = self.valueType(textvalue) 
            Logger.warning("%s : Invalid metric type provided, changing to %s based on initial value" % (metric, thetype))
            properties.type(thetype) 
        # validate initial value is of specified type and initialize to a valid value
        validvalue = self.__validValue(thetype, textvalue)
        self.__metrics[metric] =  Metric(metric, validvalue, properties)
        
    # mirrors com.hackorama.plethora.channel.PropertiesMetricsReader.getMetricProperties
    def __getMetricProperties(self, metric, props):
        metricprops = MetricProperties()
        name = self.__getAttribute(MetricAttrib.DISPLAYNAME, props, metric, metric)
        description = self.__getAttribute(MetricAttrib.DESCRIPTION, props, metric, "")
        level = self.__getLevel(self.__getAttribute(MetricAttrib.LEVEL, props, metric, Common.DEFAULT_METRIC_LEVEL))
        thetype = self.__getType(self.__getAttribute(MetricAttrib.TYPE, props, metric, MetricType.UNSPECIFIED))
        Logger.debug("Adding metric properties for " + str(metric) + " of type " +  str(thetype) + ", at level " + str(level))
        if name != metric: 
            Logger.debug("                             " + str(metric) + " with display name " +  str(name))
        if description != "": 
            Logger.debug("                             " + str(metric) + " and description " +  str(description))
        return metricprops.type(thetype).displayname(name).description(description).level(level)
    
    def __validValue(self, thetype, textvalue):
        if thetype == MetricType.NUMBER:
            value = self.__getNumber(textvalue) 
        elif thetype == MetricType.BOOLEAN:
            value = self.__getBoolean(textvalue) 
        else:
            if textvalue is None:
                value = ""
            else:
                value = textvalue
        return value
    
    def __getNumber(self, textvalue):
        if self.isNumber(textvalue):
            return long(textvalue)
        return long(0)
    
    def __getBoolean(self, textvalue):
        if textvalue is None:
            return False
        if isinstance(textvalue, bool): 
            return bool(textvalue)
        if textvalue.lower() == "true":
            return True
        return False 
    
class MetricsJSONReader(Common):
    """
    Reads metric definitions from a given JSON file
    
    Mirrors com.hackorama.plethora.channel.JSONMetricsReader
    """
    
    def __init__(self, logger=None):
        raise NotImplementedError("There be dragons ... Please implement me first ...") # TODO FIXME
        
    def getMetricsFromFile(self, propertyfile):
        """
        Read metric definitions from a JSON file
        Returns a map of all the metrics 
        """
        raise NotImplementedError("There be dragons ... Please implement me first ...") # TODO FIXME
    
class Plethora(Common):
    """
    Plethora metrics store implementation 

    Only the metric store initialization is done with locking of data store.
    All getters/setters read/write to the data store with no locking
    Provides getters/setters and helper methods (incr/decr) 
    for metrics manipulation 

    In all public methods including constructor all exceptions are caught and 
    logged by plethora to intentionally to protect the caller from side effects 
    and responsibility to handle errors upstream at the application level

    """

    def __init__(self, storename="plethora", configfile=None, logger=None):
        global Logger
        try:
            self.__metrics = {} 
            self.__metricprops = {} 
            self.__storename = storename 
            self.__CONFIG_FILE = configfile 
            self.__VERSION = "1.0.0"
            self.numbertype_lookup = set()
            self.__notnumbertype_lookup = set()
            if logger is not None: # use the new logger if provided e
                Logger = PlethoraLogger(logger) 
            self.__initConfig()
            self.__initMetricStore()
                
        except Exception: # protect caller, capture all exception and log/raise
            Logger.error("Error during metric store initialization")
            Logger.error(self.handleException())

    def getDataStore(self):
        return self.__metrics
    
    def getDataProps(self):
        return self.__metricprops
    
    def setMetric(self, name, value):
        """
        Set a metric by name, requires non-None input for both name and value
        """
        try :
            # fail fast for illegal input
            if not self.__validInputs(name, value):
                Logger.error("Illegal arguments, not setting  metric %s = %s" % (name, value))
                return False

            #  check metric store availability
            if not self.isReady():
                Logger.warning("Plethora store is not available or ready for setting metric");
                return False

            thetype =  self.__getType(name)
            if thetype is None: # did not find type information about metric, not a registered metric
                Logger.warning("Not a valid metric name \"%s\", check the metric definition in configuration file" % name);
                return False
            
            if thetype != MetricType.TEXT and self.valueType(value) != thetype: # for non text types (number/boolean) check if value is type compatible 
                Logger.warning("Not a valid value type of value \"%s\" for name \"%s\", check the metric definition in configuration file" % (value, name))
                return False
                
            #  passed all checks set the value
            self.__setStoreValue(name, value)
            return True

        #  in case of errors, protect caller, capture all exception and log/raise
        except Exception:
            Logger.error("Error during set metric %s" % name)
            Logger.error(self.handleException())
        return False

    def getMetric(self, name):
        """
        Get a metric by name , None if not found or in case of errors or illegal input
        """
        try :
            # fail fast for illegal input
            if not self.__validInput(name): # fail fast
                Logger.error("Illegal arguments, not getting  metric " + str(name))
                return None

            #  check metric store availability
            if not self.isReady():
                Logger.info("Plethora store is not available or ready for getting metric");
                return None

            # passed checks get the value
            return self.__getStoreValue(name)

        #  in case of errors, protect caller, capture all exception and log/raise
        except Exception:
            Logger.error("Error during get metric " + str(name))
            Logger.error(self.handleException())
        return None
    
    def incrMetric(self, name, delta=1):
        """
        Increment the named metric value by the given delta or by 1 if delta 
        is absent. If this is a new metric, initialize it with the delta value, 
        or with '0' if delta is invalid
        """
        try:
            value = self.getMetric(name)
            if value is not None:
                if delta is not None:
                    if self.__isNumberTypeMetric(name, value) and self.isNumber(delta): 
                        return self.setMetric(name, value + delta)
                    else:
                        Logger.error("Not a valid type of metric that can be incremented " + str(name))
                else:
                    Logger.error("Illegal argument for delta, not incrementing metric " + str(name))
            else:
                if delta is not None:
                    return self.setMetric(name, delta)
                else:
                    return self.setMetric(name, 0)
        # in case of errors, protect caller, capture all exception and log/raise
        except Exception:
            Logger.error("Error during increment metric " + str(name))
            Logger.error(self.handleException())
        return False

    def decrMetric(self, name, delta=1):
        """
        Decrement the named metric value by the given delta or by 1 if delta 
        is absent If this is a new metric, initialize it with '0' irrespective 
        of the delta value provided
        NOTE: New metric does not initialize with the delta value but as "0"
        """
        try:
            value = self.getMetric(name);
            if value is not None:
                if delta is not None:
                    if self.__isNumberTypeMetric(name, value) and self.isNumber(delta): 
                        return self.setMetric(name, value - delta)
                    else:
                        Logger.error("Not a valid type of metric that can be decremented " + str(name))
                else:
                    Logger.error("Illegal argument for delta, not decrementing metric " + str(name))
            else:
                return self.setMetric(name, 0)
        # in case of errors, protect caller, capture all exception and log/raise
        except Exception:
            Logger.error("Error during decrement metric " + str(name))
            Logger.error(self.handleException())
        return False

    def addMetric(self, name, value, properties=None):
        """
        Adds a new metric to the metric data store.
        Requires non-None name and initial value.
        Defaults to read only metric with default feature of public access
        """
        try:
            if not self.__validInputs(name, value): # fail fast
                Logger.error("Illegal arguments, not adding the new metric " + str(name) + " wth value " + str(value))
                return False

            #  check metric store availability
            if not self.isReady():
                Logger.info("Plethora store is not available or ready for adding metric");
                return None

            if self.__setStoreValue(name, value):
                if properties is None:
                    valuetype = self.valueType(value)
                    properties = MetricProperties(valuetype)
                self.__addMetricProperties(name, properties)
                Logger.info("Added metric " + str(name) + " as datastore." + str(self.__storename) + "." + str(name) + " with value " + str(value))
            else:
                Logger.error("Failed adding metric " + str(name) + " = " + str(value))
            return True
        # in case of errors, protect caller, capture all exception and log/raise
        except Exception:
            Logger.error("Error during add metric " + str(name))
            Logger.error(self.handleException())
        return False


    def addMetricProperties(self, name, thetype=Common.DEFAULT_METRIC_TYPE,  displayname=None, description="", visibility=Common.DEFAULT_METRIC_LEVEL, write=False):
        """
        Adds additional properties for a module metric already added to 
        the metric data store. Requires non-None name of an already added metric
        """
        try:
            properties = MetricProperties().type(thetype).displayname(displayname).description(description).visibility(visibility).writable(write)
            return self.__addMetricProperties(name, properties)
        except Exception:
            Logger.error("Error during adding property for metric " + str(name))
            Logger.error(self.handleException())
        return False

    def addMetricsFromFile(self, filename):
        """
        Read metric definitions from the given file and add them
        """
        metrics = MetricsPropertiesReader(None).getMetricsFromFile(filename)
        for _, metric in  metrics.items():
            self.addMetric(metric.getName(), metric.getValue(), metric.getProperties())
            
    ### protected ###
    
    def _addMetrics(self): # override for specialized metrics addition
        pass
    
    def _getDatastore(self): #override for specialized data store implementation
        pass
    
    ### private ###
    
    def __getType(self, name):
        props = self.__metricprops.get(name, None)
        if props is not None:
            return props.getType() 
        return None
    
    def __initConfig(self):
        # if config file set in environment, use that one 
        env = self.getEnvVar(self.ENV_VAR_CONFIG_FILE)
        if env is not None:
            self.__CONFIG_FILE = env 
              
        if self.__CONFIG_FILE is None:
            Logger.warning("No configuration file provided !")
            Logger.warning("(Provide configuration file during construction or please set %s)" % self.ENV_VAR_CONFIG_FILE)
                
    def __getStoreValue(self, name):
        try:
            return self.__metrics[name]
        except:
            Logger.warning("Unknown metric %s" % name)
        return None

    def __setStoreValue(self, name, value):
        self.__metrics[name] = value
        return True

    def __isStoredMetric(self, name):
        return name in self.__metrics

    # checked during initialization, do not use the standard getMetric()
    def __metricsInitialised(self):
        try :
            return self.__VERSION == self.__metrics[self.VERSIONTAG]
        except:
            return False
        
    def isReady(self):
        return self.__metricsInitialised()

    def __validMetricName(self, name):
        return name is not None

    def __addMetricVersion(self):
        self.__metrics[self.VERSIONTAG] = self.__VERSION
        Logger.info("Added metric %s with value %s" % (self.VERSIONTAG, self.__VERSION))

    def __addMetricProperties(self, name, properties):
        if not self.__isStoredMetric(name):
            Logger.error("Cannot add properties for non-existing metric \"%s\", please add it first" % name)
            return False
        self.__metricprops[name] = properties
        return True

    def __addMetricsFromConfigurationFile(self):
        self.addMetricsFromFile(self.__CONFIG_FILE)
            
    def __initMetricStore(self):
        if not self.__metricsInitialised():
            plethoralock = threading.Lock()
            Logger.info("Locking plethora to initialize the metrics for " + str(self.__storename))
            try:
                # blocked only once during initialization
                plethoralock.acquire()
                if not self.__metricsInitialised(): # double checked locking
                    self.__addMetricVersion() # must be added first
                    self.__addMetricsFromConfigurationFile() # from config file 
                    self._addMetrics() # implementation specific additional metrics
            finally:
                Logger.info("Unlocking plethora initializing the metrics for " + str(self.__storename))
                plethoralock.release()
        else:
            Logger.info("Connected to metrics store " + str(self.__storename))
        return True

    def __validInputs(self, a, b):
        return self.__validInput(a) and self.__validInput(b)

    def __validInput(self, value):
        return value is not None
    
    def __isNumberTypeMetric(self, name, value):
        # type checks optimized by memoization on first access.
        if name in self.numbertype_lookup:
            return True
        if name in self.__notnumbertype_lookup:
            return False
        if self.isNumber(value) and not self.isBoolean(value): 
            self.numbertype_lookup.add(name)
            return True
        self.__notnumbertype_lookup.add(name)
        return False
    
def test_metrics(): # testing only
    metrics = MetricsPropertiesReader(None).getMetricsFromFile("test.metrics.properties")
    for mname, mvalue in  metrics.items():
        print mname
        mvalue.debugPrint()
