/*
 * requesthandler.h
 *
 *  Created on: Jun 23, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef REQUESTHANDLER_H_
#define REQUESTHANDLER_H_

#include <string>
#include <sstream>
#include <iostream>
#include <vector>
#include <map>
#include <set>
#include <utility>
#include "autil.h"
#include "metrics.h"
#include "formatter.h"

namespace plethora {

class RequestHandler {
	static const std::string ARG_GET_METRIC;
	static const std::string ARG_DATA_FORMAT;
	static const std::string ARG_SET_METRIC;
	static const std::string ARG_METRIC_VALUE;
	static const std::string ARG_METRIC_PROPS;
	static const std::string ARG_LIST;
	static const std::string ARG_LIST_METRICS;
	static const std::string ARG_LIST_METRIC_NAMES;
	static const std::string ARG_LIST_METRIC_PROPS;

	static const std::string MSG_DEFAULT;
	static const std::string MSG_ERROR;
	static const std::string PLETHORA_TAG;

	std::map<std::string, std::string> queryArgs;
	Formatter *formatter;

	std::string validateInput(std::string input);
	bool validMetric(std::string name);
	std::string metricValue(std::string name);
	bool setMetricValue(std::string name, std::string value);

	void buildQueryArgMap(std::string request);
	bool hasArg(std::string);
	bool hasArgWithValue(std::string);
	std::string getArgValue(std::string name);

	std::string printMetrics();
	std::string printNames();
	std::string printProps(std::string name);
	std::string printProps();

	std::string handleGet();
	std::string handleSet();
	std::string handleList();
	std::string handleProps();
	std::string handleDefault();
	std::string handleError();

	std::string formatData(std::string name);
	std::string formatData(std::string name, std::string value);
	std::string formatData(std::set<std::string> list);
	std::string formatData(std::map<std::string, std::string> map);

public:
	RequestHandler(Formatter *formatter);
	~RequestHandler();
	std::string handle(std::string request);
	std::string handle(std::string request, std::string &response);
};

} /* namespace plethora */

#endif /* REQUESTHANDLER_H_ */
