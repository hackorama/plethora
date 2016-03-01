/*
 * channel.cpp
 *
 *  Created on: Jun 7, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */
#include "channel.h"

using namespace std;

namespace plethora {

const string Channel::PROP_PREFIX = "plethora";
const string Channel::PROP_HOST = PROP_PREFIX + CONFIG_KEY_SEPARATOR + "host";
const string Channel::PROP_PORT = PROP_PREFIX + CONFIG_KEY_SEPARATOR + "port";
const vector<string> Channel::ATTRIBUTES = Channel::initAttributes();
const vector<string> Channel::LEVELS = Channel::initLevels();
const vector<string> Channel::TYPES = Channel::initTypes();

/**
 *
 */
Channel::Channel() {
	Channel(NULL);
}

/**
 * Plethora agent
 *
 * @param filename
 */
Channel::Channel(const char *filename) {
	port = 0;
	readConfig(readFile(filename ? filename : ""));
	cout << "Initialized plethora at " << hostname << ":" << port <<endl;
}

Channel::~Channel() {
	Metrics::release();
}

const vector<string> Channel::initAttributes() {
	vector<string> v;
	v.push_back("name");
	v.push_back("type");
	v.push_back("level");
	v.push_back("description");
	return (v);
}

const vector<string> Channel::initLevels() {
	vector<string> v;
	v.push_back("public");
	v.push_back("protected");
	v.push_back("internal");
	return (v);
}

const vector<string> Channel::initTypes() {
	vector<string> v;
	v.push_back("number");
	v.push_back("text");
	v.push_back("boolean");
	return (v);
}

char *Channel::readFile(string filename) {
	if(filename.empty()) {
		return (false);
	}
	int length;
	ifstream is;
	is.open(filename.c_str(), ifstream::in);
	if (!is.good()) {
		is.close();
		return (false);
	}
	is.seekg(0, ios::end);
	length = is.tellg();
	is.seekg(0, ios::beg);
	char *buffer = (char*) calloc(length+1, sizeof(char));
	is.read(buffer, length);
	is.close();
	return (buffer);
}

bool Channel::readConfig(char* config) {
	if (config == NULL) {
		return (false);
	}
	char *cursor = config;
	char *pline = NULL;
	int linelength = 0;
	bool newline = true;
	for (unsigned int i = 0; i < strlen(config); i++) {
		if (*cursor == EOL) { /* end of line */
			if (pline != NULL) /* valid line */
				processLine(pline, linelength, PROP_DELIMITER);
			pline = NULL;
			newline = true;
		} else if (newline) { /* start of line */
			newline = false;
			if ((*cursor != PROP_COMMENT)) {
				pline = cursor;
				linelength = 0;
			}
		}
		cursor++;
		linelength++;
	}
	free(config);
	for(map<string, string>::iterator it = props.begin(); it != props.end(); it++) {
		processNameValuePair(it->first, it->second);
	}
	return (true);
}

void Channel::processLine(char *theline, int length, char delimiter) {
	bool found = false;
	char *pline = (char*) malloc((length + 1) * sizeof(char));
	for (int i = 0; i < length; i++)
		pline[i] = *(theline++);
	pline[length] = NULL_TERMINATE;
	char *cursor = pline;
	if (*cursor == delimiter || *(cursor + length - 1) == delimiter) {
		return;
	}
	for (int i = 1; i < length - 1 && !found; i++) {/* find delimiter */
		if (*cursor == delimiter) {
			*(cursor - 1) = NULL_TERMINATE; /* two strings */
			found = true;
			props.insert(pair<string, string>(autil::trim(autil::toLower(pline)), autil::trim(cursor + 1)));
		}
		cursor++;
	}
	free(pline);
}

void Channel::processNameValuePair(string name, string value) {
	isMetricKey(name, PROP_PREFIX) ?
	processMetricNameValue(name, value) :
	processSysNameValue(name, value);
}

void Channel::processSysNameValue(string name, string value) {
	if (PROP_HOST.compare(name) == 0) {
		hostname = value;
	} else if (PROP_PORT.compare(name) == 0) {
		port = atoi(value.c_str());
	}
}

void Channel::processMetricNameValue(const string name, const string value) {
	string metric = getMetricNameFromPropertyKey(name);
	if(!existingMetric(metric)) {
		Enums::METRICTYPE type =  getType(getAttribute(Enums::TYPE, metric, Enums::TYPEOF(Enums::UNSPECIFIED)));
		addMetric(metric, value, type, getMetricProperties(metric));
	}
}

void Channel::addMetric(std::string name, std::string textvalue, Enums::METRICTYPE type, MetricProperties *props) {
	Enums::METRICTYPE thetype = type;
	if (thetype == Enums::UNSPECIFIED) {
		thetype = valueType(textvalue);
	} else if (thetype == Enums::INVALID) {
		thetype = valueType(textvalue);
	}
	BaseMetric *metric;
	props->setType(thetype); // update the type
	switch (thetype) {
	case Enums::TEXT:
		metric = new Metric<string>(name, textvalue, props);
		break;
	case Enums::BOOLEAN:
		metric = new Metric<bool>(name, autil::getBool(textvalue), props);
		break;
	case Enums::NUMBER:
		metric = new Metric<long>(name, autil::getNumber(textvalue), props);
		break;
	default:
		metric = new Metric<string>(name, textvalue, props);
		break;
	}
	Metrics::addMetric(metric);
}

Enums::METRICTYPE Channel::valueType(std::string textvalue) {
	if (autil::isNumeric(textvalue)) {
		return (Enums::NUMBER);
	} else if (autil::isBoolean(textvalue)) {
		return (Enums::BOOLEAN);
	}
	return (Enums::TEXT);
}

MetricProperties *Channel::getMetricProperties(std::string metric) {
	MetricProperties *properties = new MetricProperties();
	string name = getAttribute(Enums::NAME,  metric, metric);
	string description = getAttribute(Enums::DESCRIPTION,  metric, "");
	Enums::METRICLEVEL level = Enums::LEVELFROM(getAttribute(Enums::LEVEL, metric));
	properties->setDisplayname(name)->setDescription(description)->setLevel(level);
	return (properties);
}

Enums::METRICTYPE Channel::getType(string type) {
	return (Enums::TYPEFROM(type));
}

string Channel::getAttribute(Enums::ATTRIBUTE attribute, string metric) {
	string result;
	map<string, string>::iterator it = props.find(metric + CONFIG_KEY_SEPARATOR + Enums::ATTRIBUTEOF(attribute));
	if(it != props.end()) {
		return (it->second);
	}
	return (result);
}

string Channel::getAttribute(Enums::ATTRIBUTE attribute, string metric, string defaultValue) {
	string result =  getAttribute(attribute, metric);
	return (result.empty() ? defaultValue : result );

}

bool Channel::isMetricKey(const string key, const string prefix) {
	if (prefix.empty()) {
		return (true);
	}
	return (!autil::startsWith(key, prefix));
}

string Channel::getMetricNameFromPropertyKey(const string key) {
	for (unsigned int i = 0; i < Channel::ATTRIBUTES.size(); i++) {
		if (autil::endsWith(key, CONFIG_KEY_SEPARATOR + ATTRIBUTES.at(i))) {
			return (key.substr(0, key.rfind(CONFIG_KEY_SEPARATOR)));
		}
	}
	return (key);
}

bool Channel::existingMetric(const string key) {
	return (Metrics::getMetricsStore().count(key));
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Channel::setMetric(const char *name, long value) {
	return (Metrics::setValue(name, value));
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Channel::setMetric(const char *name, const char *value) {
	return (Metrics::setValue(name, value));
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Channel::setMetric(const char *name, bool value) {
	return (Metrics::setValue(name, value));
}


/**
 * Increment the value for the named metric by given delta
 *
 * @param name
 * @param delta
 * @return true on success
 */
bool Channel::incrMetric(const char *name, long delta) {
	return (Metrics::setValue(name, Metrics::getNumber(name) + delta));
}

/**
 * Increment the value for the named metric by one
 *
 * @param name
 * @return true on success
 */
bool Channel::incrMetric(const char *name) {
	return (incrMetric(name, 1));
}

/**
 * Decrement the value for the named metric by given delta
 *
 * @param name
 * The metric name
 * @param delta
 * The increment to the metric value
 * @return True on success
 */
bool Channel::decrMetric(const char *name, long delta) {
	return (Metrics::setValue(name, Metrics::getNumber(name) - delta));
}

/**
 * Decrement the value for the named metric by one
 *
 * @param name
 * @return true on success
 */
bool Channel::decrMetric(const char *name) {
	return (decrMetric(name, 1));
}

/**
 * Get the value for the metric
 *
 * @param name
 * @return the metric value formatted as string
 */
const char *Channel::getMetric(const char *name) {
	return (Metrics::getStringFormated(name).c_str());
}

/**
 * Get the value for the metric
 *
 * @param name
 * @return the string metric value, or empty string on failure
 */
const char *Channel::getTextMetric(const char *name) {
	return (Metrics::getText(name).c_str());
}

/**
 * Get the value for the metric
 *
 * @param name
 * @return the number metric value, or 0 on failure
 */
long Channel::getNumberMetric(const char *name) {
	return (Metrics::getNumber(name));
}

/**
 * Get the value for the metric
 *
 * @param name
 * @return the boolean metric value
 */
bool Channel::getBooleanMetric(const char *name) {
	return (Metrics::getBool(name));
}

} /* namespace plethora */

