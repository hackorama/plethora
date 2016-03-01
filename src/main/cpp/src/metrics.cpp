/*
 * metrics.cpp
 *
 *  Created on: Jun 7, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#include "metrics.h"
#include <iostream>

using namespace std;

namespace plethora {

map<string, BaseMetric*> Metrics::METRICS;

Metrics::Metrics() {
}

Metrics::~Metrics() {
	release();
}

void Metrics::release() {
	for(map<string, BaseMetric*>::iterator it = METRICS.begin(); it != METRICS.end(); it++) {
		if(it->second != NULL) {
			delete it->second;
		}
	}
	METRICS.clear();
}

map<string, BaseMetric*> Metrics::getMetricsStore() {
	return (METRICS);
}

map<string, string> Metrics::getMetrics() {
	map<string, string> result;
	for(map<string, BaseMetric*>::iterator it = METRICS.begin(); it != METRICS.end(); it++) {
		result.insert(pair<string, string>(it->first, it->second->getValueString()));
	}
	return (result);
}

set<string> Metrics::getMetricNames() {
	set<string> result;
	for(map<string, BaseMetric*>::iterator it = METRICS.begin(); it!= METRICS.end(); it++) {
		result.insert(it->first);
	}
	return (result);
}

map<string, string> Metrics::getMetricProps() {
	map<string, string> result;
	for (map<string, BaseMetric*>::iterator it = METRICS.begin();
	        it != METRICS.end(); it++) {
		string name = it->first;
		map<string, string> props = it->second->getProperties()->asMap();
		for (map<string, string>::iterator pit = props.begin();
		        pit != props.end(); pit++) {
			result.insert(
			    pair<string, string>(name + "." + pit->first, pit->second));
		}
	}
	return (result);
}

MetricProperties *Metrics::getMetricProps(std::string name) {
	return  (isValidMetric(name) ? getMetric(name)->getProperties() : NULL);
}

bool Metrics::isValidMetric(string name) {
	return (METRICS.find(name) != METRICS.end());
}

bool Metrics::addMetric(BaseMetric *metric) {
	if (metric == NULL) {
		return (false);
	}
	METRICS.insert(make_pair(metric->getName(), metric));
	return (true);
}

BaseMetric *Metrics::getMetric(string name) {
	map<string, BaseMetric*>::iterator it = METRICS.find(name);
	return (it != METRICS.end() ? it->second : NULL);
}

long Metrics::getNumber(string name) {
	Metric<long> *m = (Metric<long>*) getMetric(name);
	return (m == NULL ? 0 : m->getValue());
}

string Metrics::getText(string name) {
	Metric<string> *m = (Metric<string>*) getMetric(name);
	return (m == NULL ? "" : m->getValue());
}

string Metrics::getStringFormated(string name) {
	BaseMetric *m = getMetric(name);
	return (m == NULL ? "" : m->getValueString());
}

bool Metrics::getBool(string name) {
	Metric<bool> *m = (Metric<bool>*) getMetric(name);
	return (m == NULL ? false : m->getValue());
}

bool Metrics::setValue(string name, long value) {
	Metric<long> *m = (Metric<long>*) getMetric(name);
	return (m == NULL ? false : m->setValue(value));
}

bool Metrics::setValue(string name, bool value) {
	Metric<bool> *m = (Metric<bool>*) getMetric(name);
	return (m == NULL ? false : m->setValue(value));
}

bool Metrics::setValue(string name, const char *value) {
	Metric<string> *m = (Metric<string>*) getMetric(name);
	return (m == NULL ? false : m->setValue(value));
}

} /* namespace plethora */

