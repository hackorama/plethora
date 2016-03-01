"""Common utilities and global definitions"""

import os, sys
import traceback
import logging

class MetricAttrib:
    """
    Metric attribute enumeration 
    """
    DISPLAYNAME = "name"
    TYPE = "type"
    LEVEL = "level"
    DESCRIPTION = "description"
    def values(self):
        return (self.DISPLAYNAME, self.TYPE, self.LEVEL, self.DESCRIPTION)

class MetricType:
    """
    Metric type enumeration 
    """
    TEXT = "text"
    NUMBER = "number"
    BOOLEAN = "boolean"
    UNSPECIFIED = "unspecified"
    INVALID = "invalid"
    def values(self):
        return (self.TEXT, self.NUMBER, self.BOOLEAN, self.UNSPECIFIED, self.INVALID)

class MetricLevel:
    """
    Metric level enumeration 
    """
    PUBLIC = "public"
    LIMITED = "limited"
    INTERNAL = "internal"
    def values(self):
        return (self.PUBLIC, self.LIMITED, self.INTERNAL)

class MetricProperties:
    """
    Metric properties chained builder for selective initialization
    """
    PROP_DISPLAYNAME = "displayname"
    PROP_DESCRIPTION = "description"
    PROP_OPTIONS = "options"
    
    def __init__(self, thetype=None):
        self.__displayname = ""
        self.__description = ""
        self.__level = Common.DEFAULT_METRIC_LEVEL
        self.__readable= True
        self.__writable = False
        if thetype is None:
            self.__type = Common.DEFAULT_METRIC_TYPE
        else:
            self.__type = thetype
        self.__options = Common().buildOptionString(self.__type, self.__level, self.__writable)

    def type(self, thetype):
        self.__type = thetype
        return self

    def displayname(self, displayname):
        self.__displayname = displayname
        return self

    def description(self, description):
        self.__description = description
        return self

    def level(self, level):
        self.__level = level
        return self

    def readable(self, readable):
        self.__readable = readable
        return self

    def writable(self, writable):
        self.__writable = writable
        return self

    def options(self, options):
        self.__options = options
        return self
    
    def getType(self):
        return self.__type

    def getDisplayname(self):
        return self.__displayname

    def getDescription(self):
        return self.__description

    def getLevel(self):
        return self.__level

    def isReadable(self):
        return self.__readable

    def isWritable(self):
        return self.__writable

    def getOptions(self):
        self.__options = Common().buildOptionString(self.__type, self.__level, self.__writable) #TODO cache
        return self.__options
    
    def asMap(self):
        result = {}
        result[self.PROP_DISPLAYNAME] = self.__displayname
        result[self.PROP_DESCRIPTION] = self.__description
        result[self.PROP_OPTIONS] = self.getOptions() 
        return result
    
class Common(object):
    """
    Common utilities used by all Plethora classes
    Holds no state data only global data definitions and utility methods 
    """
    LOGTAG = "PLETHORA"
    DEFAULT_LOG_LEVEL = logging.DEBUG
    VERSIONTAG = "VERSION" 
    PLETHORA_COMMON_NAME = "plethora"
    ENV_VAR_CONFIG_FILE = "PLETHORA_CONFIG_FILE"
    PROP_MARKER = "__PROP__"
    PLETHORA_STORE_TAG = PLETHORA_COMMON_NAME
    DEFAULT_METRIC_LEVEL = MetricLevel.PUBLIC
    DEFAULT_METRIC_TYPE = MetricType.NUMBER
    
    def getEnvVar(self, name):
        try: 
            return os.environ[name]
        except:
            return None

    def handleException(self, level=4): # handle by log/raise
        """
        Handle the exception by logging or raising appropriate application exception
        """
        try:
            exc_type, value, trace = sys.exc_info()
            msg = "Type: %s \nDescription: %s \n" % (exc_type.__name__, value)

            if level > 0:
                msg += "Stack Trace:"
                trace_list = traceback.format_tb(trace, level)
                for item in trace_list:
                    msg += "\n" + item

            return msg
        except Exception, exc: # handle the unlikely
            return str(exc) + " at Common.getStackTrace"

    def safeStr(self, o):
        """
        Safe string conversion.
        Checks for None objects and return empty string, otherwise return str(object)
        """
        if o is None:
            return ""
        return str(o)

    def sameType(self, a, b):
        """
        Verify if the two input objects are of same type.
        """
        if a is None and b is None:
            return True
        if a is None or b is None:
            return False
        return isinstance(a, type(b))

    def valueType(self, value):
        """
        Given a value checks if its a "boolean" or a "number" 
        and if neither defaults to "text" type
        """
        thetype = MetricType.TEXT
        if value is None: # returns default
            return thetype
        if self.isBoolean(value): #NOTE: must check bool before number
            thetype = MetricType.BOOLEAN
        elif self.isNumber(value):
            thetype = MetricType.NUMBER
        return thetype

    def isNumber(self, value):
        try:
            float(str(value))
            return True
        except:
            return False
        return False

    def isBoolean(self, value):
        if str(value).strip().lower() in ("true", "false"):
            return True
        return False

    def matchType(self, metrictype, value):
        """ 
        Check if a specified data type for a metric object matches the 
        actual value provided for that metric object 
        """
        if value is None: # none never matches
            return False
        if metrictype == '0': # number
            return self.isNumber(value)
        elif metrictype == '1': # text
            return True
        elif metrictype == '2': # flag
            return self.isBoolean(value)
        return False # default no match

    def buildOptionString(self, thetype=DEFAULT_METRIC_TYPE, level=DEFAULT_METRIC_LEVEL, write=False):
        """
        Build an 8 char map string of the metric options
        
        Mirrors com.hackorama.plethora.common.OptionString.build()

        0 - metric type
        1 - external write flag
        2 - external visibility level
        3 - reserved for future use
        4 - reserved for future use
        5 - reserved for future use
        6 - reserved for future use
        7 - reserved for future use
        """

        # 1. metric value type
        #
        # 0 - text (default)
        # 1 - number
        # 2 - flag
        typebit = 0
        if thetype == MetricType.NUMBER:
            typebit = 1
        if thetype == MetricType.BOOLEAN:
            typebit = 2

        # 2. external access level
        #
        # 0 - read only (default)
        # 1 - read and write
        # note : internal access is read and write always
        accessbit = 0
        if write:
            accessbit = 1

        # 3. external visibility
        #
        # 0 - public (default)
        # 1 - limited
        # 2 - internal
        levelbit = 0
        if level == MetricLevel.LIMITED:
            levelbit = 1
        if level == MetricLevel.INTERNAL:
            levelbit = 2

        # 4,5,6,7,8. future options
        futurebits  = "00000"

        return str(typebit) + str(accessbit) + str(levelbit) + str(futurebits)

class PlethoraLogger(Common):
    """
    A log formatter wrap around common module logger
    """
    def __init__(self, logger=None):
        self.__internal = False
        if logger is None:
            logging.basicConfig() #TODO customize logging
            self.__logger = logging.getLogger(self.LOGTAG)
            self.__logger.setLevel(self.DEFAULT_LOG_LEVEL) 
            self.__internal = True
        else:
            self.__logger = logger

    def __format(self, msg):
        if self.__internal: # No formatting if internal logger is used
            return msg
        return self.LOGTAG + ":" + msg 
    
    def log(self, msg):
        self.__logger.log(self.__format(msg))
    
    def debug(self, msg):
        self.__logger.debug(self.__format(msg))
    
    def info(self, msg):
        self.__logger.info(self.__format(msg))
    
    def warning(self, msg):
        self.__logger.warning(self.__format(msg))
    
    def error(self, msg):
        self.__logger.error(self.__format(msg))
    
    def critical(self, msg):
        self.__logger.critical(self.__format(msg))
    
    def exception(self, exc):
        msg = "EXCEPTION: " + str(exc)
        self.critical(msg)
