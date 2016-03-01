/*
 * metrics.h
 *
 *  Created on: Jun 7, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef METRICS_H_
#define METRICS_H_

#include <string>
#include <map>
#include <set>
#include <iterator>
#include <algorithm>
#include "metric.h"

namespace plethora {

class Metrics {
public:
	static std::map<std::string, BaseMetric*> METRICS;
	Metrics();
	virtual ~Metrics();
	static void release();
	static std::map<std::string, BaseMetric*> getMetricsStore();
	static std::map<std::string, std::string> getMetrics();
	static std::set<std::string> getMetricNames();
	static MetricProperties *getMetricProps(std::string name);
	static bool isValidMetric(std::string name);
	static long getNumber(std::string name);
	static std::string getText(std::string name);
	static std::string getStringFormated(std::string name);
	static bool getBool(std::string name);
	static BaseMetric* getMetric(std::string name);
	static std::map<std::string, std::string> getMetricProps();
	static bool addMetric(BaseMetric *metric);
	static bool setValue(std::string name, long value);
	static bool setValue(std::string name, const char *value);
	static bool setValue(std::string name, bool value);
};

} /* namespace plethora */

#endif /* METRICS_H_ */
