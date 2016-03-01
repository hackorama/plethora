"""Plethora web interface to the stored metrics"""

import urllib, cgi
try:
    import simplejson as json
except ImportError:
    import json
from plethoralib.common import Common, PlethoraLogger 

Logger = PlethoraLogger(None) 

class Metrics(Common):
    
    """ 
    Web interface for HTTP access to the plethora metrics data store
    
    An HTTP request object from the applications's server is provided
    and this web interface handles the request and provides results as HTTP 
    response. Results are read from the provided plethora data store.

    Defaults to JSON for response data format, easily extends to other formats
    """

    def __init__(self, datastore, dataprops, modulename="plethora", logger=None):
        
        global Logger
        self.__FORMATS = ["json", "text", "csv"]
        self.__DEFAULT_FORMAT = self.__FORMATS[0]

        # https://host:XXXXX/plethora.py?ARG=value&ARG=value
        self.__ARG_GET_METRIC        = "get"      # get=foo
        self.__ARG_DATA_FORMAT       = "as"       # get=foo&as=text
        self.__ARG_SET_METRIC        = "set"      # set=foo
        self.__ARG_METRIC_VALUE      = "with"     # set=foo&with=42
        self.__ARG_METRIC_PROPS      = "props"    # props=foo
        self.__ARG_LIST              = "list"     # list=xxxx
        self.__ARG_LIST_METRICS      = "metrics"  # list=metrics
        self.__ARG_LIST_METRIC_NAMES = "names"    # list=names
        self.__ARG_LIST_METRIC_PROPS = "props"    # list=props

        self.__MSG_DEFAULT = "204" # Successful, No Content
        self.__MSG_ERROR = "500"   # Internal Server Error
        self.__PLETHORA_TAG = "plethora"

        self.__format = self.__DEFAULT_FORMAT
        self.__valid = False
        self.__args = None
        self.__page = None
        
        if logger is not None: # use the new logger if provided
            Logger = PlethoraLogger(logger) 

        if datastore is None or dataprops is None:
            Logger.critical("ERROR: Illegal arguments, metric store %s will not be initialized" % modulename)
            raise ValueError("Illegal arguments")
        
        self.__metrics = datastore  
        self.__metricprops = dataprops  


    def requestHandler(self, httprequest, args=None):
        """
        Handles the HTTP requests to the stored metrics data 
        and provides the result as an HTTP response in JSON format 
        """
        if httprequest is None:
            Logger.critical("Invalid argument, cannot process the request")
            return
        if args is None:
            args = self.__getArgsFromRequest(httprequest) 
        self.__page = httprequest.wfile
        self.__args = args
        self.__format = self.__resolveDataType()
        #Logger.debug("Args : " + str(self.__args) + ", Data format : " + str(self.__format))

        self.__printHeaders(httprequest)

        if not self.__validMetricStore():
            Logger.warning("Metrics store is not initialized")
            self.__handleError()
            return

        if self.__hasArgWithValue(self.__ARG_GET_METRIC):
            self.__handleGet()
        elif self.__hasArgWithValue(self.__ARG_SET_METRIC):
            self.__handleSet()
        elif self.__hasArgWithValue(self.__ARG_LIST):
            self.__handleList()
        elif self.__hasArgWithValue(self.__ARG_METRIC_PROPS):
            self.__handleProps()
        else:
            self.__handleDefault()

    ### private implementation ###
    
    def __getArgsFromRequest(self, httprequest):
        args = {}
        qs = httprequest.path.split('?', 1) # no urlparse on python 2.4 
        if len(qs) > 1:
            args = cgi.parse_qs(qs[1])
        return args
    
    def __validMetricStore(self):
        return  self.__metrics != None

    def __validMetric(self, name):
        #return name in self.__metrics.keys()
        return name in self.__metricNames()

    def __metricValue(self, name):
        return self.__metrics.get(name, None)

    def __setMetricValue(self, name, value):
        # fail fast on illegal input
        if name is None or value is None:
            return False

        # must be valid metric
        if not self.__validMetric(name):
            Logger.warning("Not a valid metric " + str(name) + " = " + str(value))
            return False

        #  for external access must be a settable metric
        if not self.__isSettable(name):
            Logger.warning("Not a settable metric " + str(name) + " = " + str(value))
            return False

        # value must be valid type
        if not self.matchType(self.__metricType(name), value):
            Logger.warning("Not a valid value type for metric " + str(name) + " = " + str(value))
            return False

        self.__metrics[name] = value;
        return True

    def __isSettable(self, name): #TODO FIXME
        return False
    
    def __metricType(self, name): #TODO FIXME
        return  self.DEFAULT_METRIC_TYPE
    
    def __metricNames(self):
        # skip the single version tag
        return filter(lambda k: k != self.VERSIONTAG, self.__metrics.keys())

    def __metricSnap(self):
        # skip all properties and the single version tag
        return dict((k, v) for k, v in self.__metrics.iteritems() if k != self.VERSIONTAG)

    def __metricProps(self, name):
        try:
            return self.__metricprops[name].asMap()
        except:
            return {}
    
    def __metricAllProps(self):
        result = {}
        for name in self.__metricprops.keys():
            transformkeys = lambda(key, value): (name + str(".") + key, value)
            props = self.__metricProps(name)
            newprops = map(transformkeys, props.items())
            result.update(newprops)
        return result

    def __metricPropsOptions(self, name):
        return self.__metricprops[name].getOptions()
    
    def __hasArg(self, name):
        return self.__args.has_key(name)

    def __hasArgWithValue(self, name):
        return self.__hasArg(name) and len(self.__getArgValue(name))

    def __getArgValue(self, name):
        return urllib.unquote_plus(self.__args[name][0])

    def __resolveDataType(self):
        thetype = self.__DEFAULT_FORMAT
        if(self.__hasArgWithValue(self.__ARG_DATA_FORMAT)):
            thetype = self.__getArgValue(self.__ARG_DATA_FORMAT)
            if thetype not in self.__FORMATS:
                Logger.info("Unsupported format " + str(thetype) + " requested, using default format " + str(self.__DEFAULT_FORMAT))
                thetype = self.__DEFAULT_FORMAT
        return thetype;

    def __handleGet(self):
        name = self.__getArgValue(self.__ARG_GET_METRIC)
        if self.__validMetric(name):
            self.__printValue(name)
        else:
            self.__handleDefault()

    def __handleSet(self):
        name = self.__getArgValue(self.__ARG_SET_METRIC)
        if self.__validMetric(name) and self.__hasArgWithValue(self.__ARG_METRIC_VALUE):
            value = self.__getArgValue(self.__ARG_METRIC_VALUE)
            self.__setValue(name, value)
            return
        self.__handleDefault()

    def __handleList(self):
        value = self.__getArgValue(self.__ARG_LIST)
        if value == self.__ARG_LIST_METRICS:
            self.__printMetrics()
        elif value == self.__ARG_LIST_METRIC_NAMES:
            self.__printNames()
        elif value == self.__ARG_LIST_METRIC_PROPS:
            self.__printAllProps()
        else:
            self.__handleDefault()

    def __handleProps(self):
        name = self.__getArgValue(self.__ARG_METRIC_PROPS)
        if self.__validMetric(name):
            self.__printProps(name)
        else:
            self.__handleDefault()

    def __handleDefault(self):
        self.__printOut(self.__formatData(self.__MSG_DEFAULT)) # for debugging

    def __handleError(self):
        self.__printOut(self.__formatData(self.__MSG_ERROR)) # for debugging

    # output formatting #

    # value only

    def __formatText(self, data):
        return str(data)

    def __formatJSON(self, data):
        return json.dumps({self.__PLETHORA_TAG: data})

    def __formatCSV(self, data):
        return str(data)

    # name,value pair

    def __formatTextTuple(self, name, value):
        return (str(name) + " " + str(value))

    def __formatJSONTuple(self, name, value):
        return json.dumps({name: value})

    def __formatCSVTuple(self, name, value):
        return (str(name) + "," + str(value))

    # value sets

    def __formatTextSet(self, data):
        return str(data)

    def __formatJSONSet(self, data):
        return json.dumps({self.__PLETHORA_TAG: data})

    def __formatCSVSet(self, data):
        return str(data)

    # name, value pair sets

    def __formatTextTupleSet(self, data):
        return str(data)

    def __formatJSONTupleSet(self, data):
        return json.dumps(data)

    def __formatCSVTupleSet(self, data):
        return str(data)

    def __formatData(self, data):
        if self.__format == "json":
            return self.__formatJSON(data)
        elif self.__format == "text":
            return self.__formatText(data)
        elif self.__format == "csv":
            return self.__formatCSV(data)

    def __formatDataTuple(self, name, value):
        if self.__format == "json":
            return self.__formatJSONTuple(name, value)
        elif self.__format == "text":
            return self.__formatTextTuple(name, value)
        elif self.__format == "csv":
            return self.__formatCSVTuple(name, value)

    def __formatDataSet(self, data):
        if self.__format == "json":
            return self.__formatJSONSet(data)
        elif self.__format == "text":
            return self.__formatTextSet(data)
        elif self.__format == "csv":
            return self.__formatCSVSet(data)

    def __formatDataTupleSet(self, data):
        if self.__format == "json":
            return self.__formatJSONTupleSet(data)
        elif self.__format == "text":
            return self.__formatTextTupleSet(data)
        elif self.__format == "csv":
            return self.__formatCSVTupleSet(data)

    # output #

    def __printHeaders(self, httprequest):
        httprequest.send_response( 200, "OK")
        httprequest.send_header( "Content-type", "application/json")
        httprequest.end_headers()

    def  __printOut(self, data):
        self.__page.write(str(data))

    def  __printValue(self, name):
        value = self.__formatDataTuple(name, self.__metricValue(name))
        self.__printOut(value)

    def  __setValue(self, name, value):
        self.__setMetricValue(name, value)
        self.__printValue(name)

    def  __printNames(self):
        value = self.__formatDataSet(self.__metricNames())
        self.__printOut(value)

    def  __printMetrics(self):
        value = self.__formatDataTupleSet(self.__metricSnap())
        self.__printOut(value)

    def  __printProps(self, name):
        value = self.__formatDataTupleSet(self.__metricProps(name))
        self.__printOut(value)
        
    def  __printAllProps(self):
        value = self.__formatDataTupleSet(self.__metricAllProps())
        self.__printOut(value)
