/*
 * channel.h
 *
 *  Created on: Jun 7, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef CHANNEL_H_
#define CHANNEL_H_

#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <cstring>
#include <string>
#include <vector>

#include "enums.h"
#include "autil.h"
#include "metric.h"
#include "metrics.h"
#include "requesthandler.h"

namespace plethora {

class Channel {
	static const char CONFIG_KEY_SEPARATOR = '.';
	static const char PROP_DELIMITER = ':';
	static const char PROP_COMMENT = '#';
	static const char WHITE_SPACE = ' ';
	static const char EOL = '\n';
	static const char NULL_TERMINATE = '\0';

	static const std::string PROP_PREFIX;
	static const std::string PROP_HOST;
	static const std::string PROP_PORT;
	static const std::vector<std::string> ATTRIBUTES;
	static const std::vector<std::string> LEVELS;
	static const std::vector<std::string> TYPES;

	std::string name;
	std::string hostname;
	int port;
	std::map<std::string, std::string> props;

	static const std::vector<std::string> initAttributes();
	static const std::vector<std::string> initLevels();
	static const std::vector<std::string> initTypes();
	bool readConfig(char *config);
	char *readFile(std::string filename);
	void processLine(char *theline, int length, char delimiter);
	void processNameValuePair(const std::string name, const std::string value);
	void processSysNameValue(const std::string name, const std::string value);
	void processMetricNameValue(const std::string name,
	                            const std::string value);
	std::string getMetricNameFromPropertyKey(const std::string key);
	bool isMetricKey(const std::string key, const std::string prefix);
	bool existingMetric(const std::string key);
	void log(const std::string msg);
	std::string getAttribute(Enums::ATTRIBUTE attribute, std::string metric, std::string defaultValue);
	std::string getAttribute(Enums::ATTRIBUTE attribute, std::string metric);
	Enums::METRICTYPE getType(std::string type);
	Enums::METRICTYPE valueType(std::string textvalue);
	void addMetric(std::string metric, std::string textvalue, Enums::METRICTYPE type, MetricProperties *props);
	MetricProperties *getMetricProperties(std::string metric);

public:
	Channel();
	Channel(const char *filename);
	virtual ~Channel();

	const char *getMetric(const char *name);
	const char *getTextMetric(const char *name);
	long getNumberMetric(const char *name);
	bool getBooleanMetric(const char *name);
	bool setMetric(const char *name, long value);
	bool setMetric(const char *name, bool value);
	bool setMetric(const char *name, const char *value);
	bool incrMetric(const char *name, long delta);
	bool incrMetric(const char *name);
	bool decrMetric(const char *name, long delta);
	bool decrMetric(const char *name);
};

} /* namespace plethora */

#endif /* CHANNEL_H_ */
